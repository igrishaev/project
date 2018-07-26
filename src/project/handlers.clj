(ns project.handlers
  "https://www.jsonrpc.org/specification"
  (:require [project.models :as models
             :refer (clean-feed clean-user)]
            [project.db :as db]
            [project.error :as e]
            [project.sync :as sync]
            [project.time :as t]
            [project.env :refer (env)]
            [project.search :as search]
            [project.opml :as opml]
            [project.resp :refer [ok] :as r]
            [project.queue :as mq]

            [clojure.tools.logging :as log]))

;;
;; API
;;


(defn subscribe
  [params user & _]
  (db/with-tx
    (let [{:keys [feed_id title]} params
          {user_id :id} user

          limit 100] ;; todo config or the last number

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
              :limit limit})

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
        {user_id :id} user
        where {:feed_id feed_id :user_id user_id}]

    (db/with-tx
      (db/unsub-del-sub where)
      (db/unsub-del-messages where))

    (ok {:feed_id feed_id})))


(def into-map (partial into {}))


(defn subscriptions
  [params user & _]
  (let [feeds (db/get-user-feeds {:user_id (:id user)})]
    (ok (map clean-feed feeds))))

;;
;; Messages
;;

(def entry-page-limit)

(defn messages
  [params user & _]
  (let [{:keys [feed_id offset]} params
        {user_id :id} user
        sub (models/find-sub
             {:feed_id feed_id :user_id user_id})

        {:keys [ordering unread_only]} sub

        ordering (or ordering "new_first")
        limit (:ui-entry-page-limit env)
        offset (or offset 0)

        query {:feed_id feed_id
               :user_id user_id
               :ordering ordering
               :unread_only unread_only
               :limit limit
               :offset offset}

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


(defn import-opml
  [params user & _]
  (let [{:keys [opml]} params
        {user_id :id} user

        ]
    (try
      (let [feed-maps (opml/read-feeds-from-string opml)]

        (if (not (empty? feed-maps))

          (db/with-tx

            (let [url->title (into {} (map (juxt :url :title) feed-maps))

                  urls (map :url feed-maps)

                  feeds (models/upsert-feeds
                         (for [url urls]
                           {:url_source url}))

                  subs (models/upsert-subs
                        (for [feed feeds
                              :let [{feed_id :id :keys [url_source]} feed]]
                          {:feed_id feed_id
                           :user_id user_id
                           :title (get url->title url_source)}))]

              (sync/sync-user user_id)

              (doseq [feed feeds]
                (mq/send {:action :sync-feed-and-user
                          :user-id user_id
                          :feed-id (:id feed)
                          :feed-url (:url_source feed)}))

              (let [feeds (db/get-user-feeds {:user_id user_id})]
                (ok (map clean-feed feeds)))))

          (r/err 400 "We could not find any feeds in the file you uploaded.")))

      (catch Throwable e
        (log/errorf "OPML import error: %s" (e/exc-msg e))
        (r/err 400 "Cannot read the OPML data you provided.")))))
