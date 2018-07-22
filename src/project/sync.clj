(ns project.sync
  (:require [project.db :as db]
            [project.models :as models]
            [project.error :as e]
            [project.time :as t]
            [project.feed :as feed]

            [clojure.tools.logging :as log]))


;;
;; Feed
;;


(defn sync-feed
  [feed]

  (let [{feed-url :url_source feed-id :id} feed]

    (log/infof "Start syncing feed: %s %s" feed-id feed-url)

    (let [fetch-result (feed/fetch-feed feed)
          {feed-db :feed entries-db :entries} fetch-result]

      (db/with-tx

        (models/update-feed feed-id feed-db)

        (when-not (empty? entries-db)
          (models/upsert-entries
           (for [e entries-db]
             (assoc e :feed_id feed-id))))))))

(defn sync-feed-safe
  [feed]
  (let [{feed-id :id
         :keys [url_source
                sync_count_err
                sync_count_total
                sync_date_next
                sync_interval]} feed
        fields (transient {})]

    (try
      (sync-feed feed)
      nil

      (catch Throwable e
        (let [err-msg (e/exc-msg e)]

          (log/errorf "Feed sync error, id: %s, url: %s, e: %s"
                      feed-id url_source err-msg)
          (assoc!
           fields
           :sync_count_err (inc sync_count_err)
           :sync_error_msg err-msg)))

      (finally
        (assoc!
         fields
         :updated_at (t/now)
         :sync_date_last (t/now)
         :sync_date_next (t/ahead sync_interval)
         :sync_count_total (inc sync_count_total))

        (let [fields (persistent! fields)]
          (models/update-feed feed-id fields))

        (db/sync-feed-entry-count {:feed_id feed-id})
        (db/sync-feed-sub-count {:feed_id feed-id})))))

(defn sync-feed-by-url
  [url]
  (let [feed (models/get-or-create-feed url)]
    (sync-feed-safe feed)))


;;
;; Users
;;


(defn sync-user
  [user_id]
  (log/infof "Start syncing user %s" user_id)

  (db/with-tx
    (db/sync-user-new-messages {:user_id user_id})
    (db/sync-subs-counters {:user_id user_id})
    (db/sync-user-sync-counters {:user_id user_id})))


;;
;; Dev import
;;

(defn feed-import
  []
  (with-open [rdr (clojure.java.io/reader "rss.txt")]
    (doseq [url (line-seq rdr)]
      (println url)
      (sync-feed-by-url url))))
