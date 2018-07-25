(ns project.handlers
  "https://www.jsonrpc.org/specification"
  (:require [project.models :as models :refer (clean-feed
                                               clean-user)]
            [project.db :as db]
            [project.sync :as sync]
            [project.time :as t]
            [project.search :as search]
            [project.resp :refer [ok] :as r]))


;;
;; API
;;


(defn subscribe
  [params user & _]
  (db/with-tx
    (let [{:keys [feed_id title]} params
          {user_id :id} user]

      (db/with-tx

        (if-let [feed (models/get-feed-by-id feed_id)]

          (do

            ;; create subscription
            (models/upsert-sub {:feed_id feed_id
                                :user_id user_id
                                :title title})

            ;; subscribe to the latest N entries
            (db/subscribe-user-to-the-last-feed-entries
             {:user_id user_id
              :feed_id feed_id
              :limit 10})

            ;; TODO take the number from the last update

            (db/sync-subs-counters
             {:user_id user_id
              :feed_id feed_id})

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

        ordering (or ordering "new_first")

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

  (let [{:keys [entry_id is_read]} params
        {user_id :id} user]

    (db/with-tx

      (if-let [entry (models/get-entry-by-id entry_id)]

        (let [{entry_id :id :keys [feed_id]} entry

              message (models/find-message
                       {:entry_id entry_id :user_id user_id})

              {message_id :id} message

              date_read (when is_read (t/now))
              delta_unread (if is_read -1 1)
              delta_total (if message 0 1)]

          (if message
            (db/update! :messages
                        {:updated_at (t/now)
                         :is_read is_read
                         :date_read date_read}
                        ["id = ?" message_id])

            (db/insert! :messages
                        {:entry_id entry_id
                         :user_id user_id
                         :is_read is_read
                         :date_read date_read}))

          (db/bump-subs-counters
           {:feed_id feed_id
            :delta_unread delta_unread
            :delta_total delta_total})

          (ok (db/get-full-entry {:entry_id entry_id})))

        (r/err-entry-404 entry_id)))))


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


(defn search-feeds
  [params user & _]
  (let [{:keys [term]} params
        {user_id :id} user
        feeds (search/search term)]
    (ok feeds)))


(defn logout
  [params user session & _]
  (let [session* (dissoc session :user-id)]
    (-> (ok {:ok true})
        (assoc :session session*))))
