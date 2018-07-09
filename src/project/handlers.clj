(ns project.handlers
  "https://www.jsonrpc.org/specification"
  (:require [project.models :as models]
            [project.db :as db]
            [project.sync :as sync]
            [project.resp :refer [ok] :as r]))

;;
;; API
;;

(def feed-fields
  [:id
   :created_at

   :updated_at

   :url_source
   :url_host
   :url_favicon
   :url_image

   :language

   :title
   :subtitle

   :link

   :date_updated_at

   :sync_interval
   :sync_date_last
   :sync_date_next
   :sync_count_total
   :sync_count_err

   :entry_count_total
   :sub_count_total])

(defn clean-feed
  [feed]
  (select-keys feed feed-fields))

;; todo check if a URL points to a feed
;; todo handle a case when it's not a feed
;; entries are empty
;; http >= 400

(defn preview
  [params & _]
  (let [{:keys [url]} params
        feed (models/get-feed-by-url url)]
    (if feed

      (if (:deleted feed)
        (r/err-feed-deleted)
        (ok (clean-feed feed)))

      (let [feed (models/create-feed url)
            {feed-id :id} feed]
        (sync/sync-feed-safe feed)
        (let [feed (models/get-feed-by-id feed-id)]
          (ok (clean-feed feed)))))))

;; todo not upsert but check if subscribed

(defn subscribe
  [params user & _]
  (db/with-tx
    (let [{:keys [feed_id title]} params
          feed (models/get-feed-by-id feed_id)

          resp (models/subscribe user feed title)

          sub-id (-> resp first :id)
          sub (models/get-sub-by-id sub-id)]
      (ok sub))))


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
