(ns project.queue
  (:require [project.error :as e]
            [project.env :refer [env]]

            [amazonica.aws.sqs :as sqs]
            [cheshire.core :as json]

            [clojure.tools.logging :as log]))

;;
;; Actions
;;


(defmulti action
  (fn [data]
    (when (map? data)
      (-> data :action keyword))))


(defmethod action :default
  [data]
  (log/errorf "Queue WARNING: unknown data, %s" data))


;;
;; Wrappers
;;


(def aws-cred
  {:access-key (:aws-access-key env)
   :secret-key (:aws-secret-key env)
   :endpoint (:aws-region env)})


(def queue-name
  (:aws-queue-name env))


(def queue-url)


(defn set-queue-url!
  [url]
  (log/infof "Setting queue URL to %s" url)
  (alter-var-root #'queue-url (constantly url)))


(defn uuid []
  (str (java.util.UUID/randomUUID)))


(defn ->message
  [group data]
  {:id (uuid)
   :message-body (json/generate-string data)
   :message-group-id group
   :message-deduplication-id (-> data hash str)})


(defn purge!
  []
  (sqs/purge-queue aws-cred :queue-url queue-url))


(def msg-limit 10)

(def chunk-messages
  (partial partition msg-limit msg-limit []))


(defn send-messages
  [group message-list]
  (doseq [messages (chunk-messages message-list)]

    (log/infof "Sending messages, group: %s, data: %s" group messages)

    (let [res (sqs/send-message-batch
               aws-cred
               :queue-url queue-url
               :entries (mapv (partial ->message group) messages))]

      (log/infof "Sent response: %s" res))))


;;
;; Worker
;;


(def beat-sleep-time 10)

(def beat-wait-time 20)

(def beat-message-count 10)


(defn process-message
  [{:keys [message-id body]}]
  (try
    (log/infof "Processing message, id: %s, body: %s"
               message-id body)

    (action (json/parse-string body true))

    (catch Throwable e
      (log/infof "Message failed, error: %s, data: %s"
                 (e/exc-msg e) body))))


(defn worker
  []

  (while true

    (try

      (let [res (sqs/receive-message
                 aws-cred
                 :queue-url queue-url
                 :wait-time-seconds beat-wait-time
                 :max-number-of-messages beat-message-count
                 :delete true)

            {:keys [messages]} res]

        (log/infof "Worker beat, %s messages received" (count messages))

        (doseq [message messages]
          (future
            (process-message message))))

      (catch Throwable e
        (log/errorf "Worker beat error: %s" (e/exc-msg e)))

      (finally
        (Thread/sleep (* 1000 beat-sleep-time))))))


(defonce __state (atom nil))

(def set-state! (partial reset! __state))


(defn start
  []
  (set-state!
   (future
     (worker))))


(defn stop
  []
  (when-let [f @__state]
    (while (not (realized? f))
      (future-cancel f))
    (set-state! nil)))

;;
;; Init
;;


(defn init-queue
  []
  (if-let [url (sqs/find-queue aws-cred queue-name)]
    (set-queue-url! url)
    (let [res (sqs/create-queue
               aws-cred
               :queue-name queue-name
               :attributes {:FifoQueue true})
          {url :queue-url} res]
      (set-queue-url! url))))


(defn init
  []
  (init-queue)
  (start))
