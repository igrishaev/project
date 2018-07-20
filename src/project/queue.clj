(ns project.queue
  (:require [taoensso.carmine :as car :refer (wcar)]
            [taoensso.carmine.message-queue :as car-mq]))


(def conn {:pool {} :spec {:uri "redis://127.0.0.1:6379"}})

(defmacro wcar*
  [& body]
  `(car/wcar conn ~@body))

(defonce worker
  (car-mq/worker
   {:pool {} :spec {}}
   "queue"
   {:auto-start false
    :handler (fn [{:keys [message attempt]}]
               (println "Received" message)
               {:status :success})}))

;; (wcar* (car-mq/enqueue "queue" "my message!"))

;; (car-mq/start worker)

;; (car-mq/stop worker)
