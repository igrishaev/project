(ns project.handlers
  (:require [project.models :as models]
            [project.db :as db]
            [project.sync :as sync]))

(defn preview
  [request]
  (let [{:keys [params]} request
        {:keys [url]} params
        feed (models/get-feed-by-url url)
        ]
    (if feed
      {:data feed}
      (do
        (sync/sync-feed url)
        {:data feed}))))


(defn subscribe
  [request]
  (db/with-tx
    (let [{:keys [params user title]} request
          {:keys [url]} params
          feed (models/get-feed-by-id url)
          resp (models/subscribe user feed title)
          sub-id (-> resp first :id)
          sub (models/get-sub-by-id sub-id)]
      {:data sub})))

(defn unsubscribe
  [request]
  (db/with-tx
    (let [{:keys [params user]} request
          {user-id :id} user
          {:keys [sub-id]} params]
      (models/unsubscribe user-id sub-id)
      {:data true})))

(defn subscriptions
  [request]
  (let [{:keys [user]} request
        subs (models/get-user-subs user)]
    {:data subs}))


(defn messages
  [request]
  (let [{:keys [params user]} request
        {user-id :id} user
        {:keys [sub-id]} params
        messages (models/get-messages user-id sub-id)]
    {:data messages}))


(defn mark-read
  [request]
  (let [{:keys [params user]} request
        {:keys [msg-ids]} params]
    (models/mark-read user msg-ids)
    {:ok true}))

(defn mark-unread
  [request]
  (let [{:keys [params user]} request
        {:keys [msg-ids]} params]
    (models/mark-unread user msg-ids)
    {:ok true}))
