(ns project.beat
  (:require [project.db :as db]
            [project.models :as models]
            [project.sync :as sync]))

(defn beat-feeds
  []
  (let [feeds (sync/get-feeds-to-sync)]
    (doseq [feed feeds]
      (sync/sync-feed-safe feed))))

(defn beat-users
  []
  (let [users (sync/get-users-to-sync)]
    (doseq [user users]
      (sync/sync-user-safe user))))

(defn sleep [sec]
  (Thread/sleep (* sec 1000)))

(defn to-task
  ;; todo log
  [func step]
  (fn []
    (future
      (while true
        (try
          (func)
          (catch Throwable e
            (println e))
          (finally
            (sleep step)))))))

(def task-users (to-task beat-users 300))

(def task-feeds (to-task beat-feeds 300))

(defn cancel
  [fut]
  (when-not (realized? fut)
    (while (not (future-cancelled? fut))
      (future-cancel fut))))

(defonce state nil)

(defn status []
  (not-empty state))

(defn alter-state
  [val]
  (alter-var-root
   #'state
   (constantly val)))

(defn start []
  (alter-var-root
   (alter-state
    [(task-users)
     (task-feeds)])))

(defn stop []
  (when status
    (doseq [f state]
      (cancel f))
    (alter-state nil)))
