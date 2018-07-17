(ns project.handlers
  "https://www.jsonrpc.org/specification"
  (:require [project.models :as models]
            [project.db :as db]
            [project.sync :as sync]
            [project.time :as t]
            [project.resp :refer [ok] :as r]))

;;
;; API
;;

(defn clean-model
  [fields model]
  (select-keys model fields))

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
   :entry_count_total
   :sub_count_total
   :sub])

(def user-fields
  [:id
   :created_at
   :updated_at
   :email
   :name
   :source
   :source_id
   :source_url
   :locale
   :avatar_url
   :gender])

(def clean-feed (partial clean-model feed-fields))

(def clean-user (partial clean-model user-fields))

;; todo check if a URL points to a feed
;; todo handle a case when it's not a feed
;; entries are empty
;; http >= 400

(defn search-wrap-feed
  [feed]
  (let [entries (db/get-last-entries {:feed_id (:id feed)})]
    (-> (clean-feed feed)
        (assoc :entries entries))))

(defn search-feeds
  [params & _]
  (let [{:keys [url]} params
        feed (models/get-feed-by-url url)]

    (if feed
      (ok (map search-wrap-feed [feed]))

      (let [feed (models/create-feed url)
            {feed-id :id} feed]

        (sync/sync-feed-safe feed)

        (let [feed (models/get-feed-by-id feed-id)]
          (ok (map search-wrap-feed [feed])))))))

;; todo add some messages

(defn subscribe
  [params user & _]
  (db/with-tx
    (let [{:keys [feed_id title]} params
          {user_id :id} user]

      (db/with-tx

        (if-let [feed (models/get-feed-by-id feed_id)]

          (do ;; todo transaction?

            ;; create subscription
            (models/upsert-sub {:feed_id feed_id
                                :user_id user_id
                                :title title})

            ;; subscribe to the latest N entries
            (db/subscribe-user-to-the-last-feed-entries
             {:user_id user_id
              :feed_id feed_id
              :limit 10})

            (let [feed (db/get-single-full-feed
                        {:feed_id feed_id :user_id user_id})]
              (ok (clean-feed feed))))

          (r/err-feed-404 feed_id))))))


(defn unsubscribe
  [params user & _]

  (let [{:keys [feed_id]} params
        {user_id :id} user]

    (db/delete!
     :subs ["feed_id = ? and user_id = ?"
            feed_id user_id])

    (ok {:feed_id feed_id})))


(def into-map (partial into {}))

(defn subscriptions
  [params user & _]
  (let [feeds (db/get-user-feeds {:user_id (:id user)})]
    (ok (map clean-feed feeds))))

;;
;; Messages
;;

(defn messages
  [params user & _]
  (let [{:keys [feed_id last_id]} params
        {user_id :id} user
        sub (models/find-sub
             {:feed_id feed_id :user_id user_id})

        {:keys [ordering unread_only]} sub

        ordering (or ordering "old_first")

        query {:feed_id feed_id
               :user_id user_id
               :ordering ordering
               :unread_only unread_only
               :last_id last_id
               :limit 3}

        entries (db/get-subscribed-entries query)]

    (ok entries)))


(defn mark-read
  [params user & _]

  ;; TODO re-calc unread count

  (let [{:keys [entry_id is_read]} params
        {user_id :id} user]

    ;; todo just update

    (if-let [entry (models/get-entry-by-id entry_id)]

      (let [date_read (when is_read (t/now))
            params {:entry_id entry_id
                    :user_id user_id
                    :is_read is_read
                    :date_read date_read}
            message (models/upsert-message params)]

        ;; todo udpate response
        (ok (assoc entry :message message)))

      (r/err-entry-404 entry_id))))


(defn user-info
  [params user & _]
  (ok (when user
        (clean-user user))))


(defn update-subscription
  [params user & _]
  (let [{:keys [feed_id]} params
        {user_id :id} user
        fields [:layout :auto_read :ordering :unread_only]
        values (not-empty (select-keys params fields))
        where ["feed_id = ? and user_id = ?" feed_id user_id]]

    (when values
      (db/update! :subs values where))

    ;; todo shortcut

    (let [feed (db/get-single-full-feed
                {:feed_id feed_id :user_id user_id})]
      (ok (clean-feed feed)))))
