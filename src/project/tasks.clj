(ns project.tasks
  (:require [project.db :as db]
            [project.queue :as queue]
            [project.sync :as sync]

            [clojure.tools.logging :as log]))


(def feeds-limit 1000)

(def users-limit 1000)

(def group "outtake.tasks")

;;
;; Tasks
;;

(defn sync-feeds-batch
  []
  (db/with-tx
    (let [feeds (db/rotate-feeds-to-update
                 {:limit feeds-limit})]

      (log/infof "%s feeds found to update" (count feeds))

      (queue/send-messages
       group (for [feed feeds]
               {:action :sync-feed
                :feed-id (:id feed)
                :feed-url (:url_source feed)})))))


(defn sync-users-batch
  []
  (db/with-tx
    (let [users (db/rotate-users-to-update
                 {:limit users-limit})]

      (log/infof "%s users found to update" (count users))

      (queue/send-messages
       group (for [user users]
               {:action :sync-user
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
    (sync/sync-user user-id)))


(defmethod queue/action :sync-feed-and-sub
  [data]
  (let [{:keys [feed-id feed-url user-id]} data]
    (db/with-tx

      (sync/sync-feed-by-url feed-url)

      (db/sync-user-new-messages
       {:user_id user-id :feed_id feed-id})

      (db/sync-subs-counters
       {:user_id user-id :feed_id feed-id}))))
