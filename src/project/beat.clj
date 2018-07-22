(ns project.beat
  (:require [project.tasks :as tasks]
            [project.error :as e]

            [clojure.tools.logging :as log]))


(defn sleep [sec]
  (Thread/sleep (* sec 1000)))


(def tasks
  [{:label "Sync-Feeds-Batch"
    :func tasks/sync-feeds-batch}

   {:label "Sync-Users-Batch"
    :func tasks/sync-users-batch}])


(def beat-step (* 60 5))


(defn beat
  []
  (while true
    (doseq [{:keys [label func]} tasks]

      (future
        (try
          (log/infof "Starting task %s" label)
          (func)
          (catch Throwable e
            (log/errorf
             "Task error, %s, %s"
             label (e/exc-msg e))))))

    (sleep beat-step)))


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
