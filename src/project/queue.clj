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
      (:action data))))


(defmethod action :default
  [data]
  (log/errorf "MQ warn: unknown data, %s" data))


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


(defn send-messages
  [group data-list]
  (log/infof "Sending messages, group: %s, data: %s" group data-list)
  (let [res (sqs/send-message-batch
             aws-cred
             :queue-url queue-url
             :entries (mapv (partial ->message group) data-list))]
    (log/infof "Sent response: %s" res)))


;;
;; Worker
;;


(def beat-sleep-time 30)

(def beat-wait-time 20)

(def beat-message-count 10)


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

        (doseq [{:keys [message-id body]} messages]

          (future
            (try
              (log/infof "Processing message, id: %s, body:  %s "
                         message-id body)

              (action (json/parse-string body true))

              (catch Throwable e
                (log/infof "Message failed, error: %s, data: %s"
                           (e/exc-msg e) body))))))

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
               :attributes {:fifo-queue true})
          {url :queue-url} res]
      (set-queue-url! url))))


(defn init
  []
  (init-queue)
  (start))
