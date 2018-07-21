(ns project.tasks
  (:require [project.db :as db]
            [project.queue :as queue]
            [project.sync :as sync]

            [clojure.tools.logging :as log]))


(def feeds-limit 10)

(def users-limit 10)

;;
;; Tasks
;;

(defn sync-feeds-batch
  []
  (db/with-tx
    (let [feeds (db/rotate-feeds-to-update
                 {:limit feeds-limit})]

      (doseq [feed feeds]
        (queue/send {:action :sync-feed
                     :feed-id (:id feed)
                     :feed-url (:url_source feed)})))))


(defn sync-users-batch
  []
  (db/with-tx
    (let [users (db/rotate-users-to-update
                 {:limit users-limit})]

      (doseq [user users]
        (queue/send {:action :sync-user
                     :user-id (:id user)})))))


;;
;; Actions
;;

(defmethod queue/action :sync-feed
  [data]
  (let [{:keys [feed-url]} data]
    (sync/sync-feed-by-url feed-url)))


(defmethod queue/action :sync-user
  [data]
  (let [{:keys [user-id]} data]
    (sync/sync-user-safe user-id)))
