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

      (ok (clean-feed feed))

      (let [feed (models/create-feed url)
            {feed-id :id} feed]
        (sync/sync-feed-safe feed)
        (let [feed (models/get-feed-by-id feed-id)]
          (ok (clean-feed feed)))))))

(defn subscribe
  [params user & _]
  (db/with-tx
    (let [{:keys [feed_id title]} params
          fields {:title title}]

      (db/with-tx

        (if-let [feed (models/get-feed-by-id feed_id)]

          (if (models/subscribed? user feed)

            (r/err-subscribed)

            (let [sub (models/subscribe user feed fields)]
              (ok sub)))

          (r/err-feed-404 feed_id))))))


(defn unsubscribe
  [params user & _]

  (let [{:keys [sub_id]} params]

    (db/with-tx

      (if (models/has-sub? user sub_id)

        (do
          (models/unsubscribe user sub_id)
          (r/ok-empty))

        (r/err-not-subscribed)))))


(defn subscriptions
  [params user & _]
  (let [subs (models/get-user-subs user)]
    (ok subs)))

;;
;; Messages
;;

(defn messages
  [params user & _]
  (let [{user-id :id} user
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
