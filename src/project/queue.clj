(ns project.queue
  (:require [project.error :as e]
            [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]

            [clojure.tools.logging :as log]))


(defmulti action
  (fn [data]
    (when (map? data)
      (:action data))))


(defmethod action :default
  [data]
  (log/errorf "MQ warn: unknown data, %s" data))


(def conn {:pool {}
           :spec {:host "127.0.0.1"
                  :port 6379
                  :user nil
                  :password nil}})


(def queue "queue")


(defn worker-handler
  [{:keys [message mid attempt]}]

  (try
    (action message)
    (log/infof "MQ: mid %s processed, attempt: %s"
               mid attempt)
    {:status :success}

    (catch Throwable e
      (log/errorf "MQ error: %s, mid: %s, message: %s, attempt: %s"
                  (e/exc-message e) mid message attempt)

      {:status :error :throwable e})))


(defonce worker
  (car-mq/worker
   conn queue
   {:auto-start false
    :handler worker-handler}))


(defmacro wcar*
  [& body]
  `(car/wcar conn ~@body))


(defn send
  [data]
  (wcar* (car-mq/enqueue queue data)))


(defn start
  []
  (car-mq/start worker))

(defn stop
  []
  (car-mq/stop worker))

;;
;; Init
;;

(defn init
  []
  (start))
