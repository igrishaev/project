(ns project.beat
  (:require [project.tasks :as tasks]
            [project.error :as e]

            [clojure.tools.logging :as log]))


(defn sleep [sec]
  (Thread/sleep (* sec 1000)))


(def tasks
  {:sync-feeds-batch
   {:func tasks/sync-feeds-batch}

   :sync-users-batch
   {:func tasks/sync-users-batch}})


(def beat-step (* 60 5))


(defn run-task
  [label]
  (if-let [func (some-> tasks (get label) :func)]
    (try
      (log/infof "Starting task %s" label)
      (func)
      (catch Throwable e
        (log/errorf
         "Task error, %s, %s"
         label (e/exc-msg e))))
    (log/errorf "No such a task: %s" label)))


(defn beat
  []
  (while true
    (doseq [label (keys tasks)]
      (future (run-task label)))
    (sleep beat-step)))


;;
;; HTTP API
;;

(defn cron-handler
  [request]
  (if-let [api (some-> request :params :api keyword)]
    (if (contains? tasks api)
      (do
        (future (run-task api))
        {:status 200 :body "OK"})
      {:status 404 :body "Task not found"})
    {:status 400 :body "Bad task parameter"}))


;;
;; Controls
;;

(defonce __state (atom nil))


(defn cancel
  [f]
  (while (not (realized? f))
    (future-cancel f)))


(defn start []
  (when-not @__state
    (reset! __state (future (beat)))
    true))


(defn stop []
  (when-let [f @__state]
    (cancel f)
    (reset! __state nil)
    true))

;;
;; Init
;;

(defn init
  []
  (start))
