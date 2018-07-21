(ns project.sync
  (:require
            [project.db :as db]
            [project.models :as models]
            [project.error :as e]
            [project.time :as t]
            [project.feed :as feed]

            [clojure.tools.logging :as log]
            [medley.core :refer [distinct-by]]))


;;
;; Feed
;;


(defn sync-feed
  [feed]
  (let [{feed-url :url_source feed-id :id} feed
        fetch-result (feed/fetch-feed feed)
        {feed-db :feed entries-db :entries} fetch-result]

    (db/with-tx

      (models/update-feed feed-id feed-db)

      (when-not (empty? entries)
        (models/upsert-entries
         (for [e entries-db]
           (assoc e :feed_id feed-id))))

      (db/sync-feed-entry-count {:feed_id feed-id})
      (db/sync-feed-sub-count {:feed_id feed-id}))

    ))

(defn sync-feed-safe
  [feed]
  (let [{:keys [id
                url_source
                sync_count_err
                sync_count_total
                sync_date_next
                sync_interval]} feed
        fields (transient {})]

    (try
      (sync-feed feed)

      (catch Throwable e
        (let [err-msg (e/exc-msg e)]

          (log/errorf "Sync error, feed: %s, e: %s"
                      url_source err-msg)
          (assoc!
           fields
           :sync_count_err (inc sync_count_err)
           :sync_error_msg err-msg)))

      ;; todo move these fields up

      (finally
        (assoc!
         fields
         :updated_at (t/now)
         :sync_date_last (t/now)
         :sync_date_next (t/ahead sync_interval)
         :sync_count_total (inc sync_count_total))

        (let [fields (persistent! fields)]
          (models/update-feed id fields))))))

(defn sync-feed-by-url
  [url]
  (let [feed (models/get-or-create-feed url)]
    (sync-feed-safe feed)))


;;
;; Users
;;


(defn sync-user
  ;; todo wrap with log etc
  [user_id]
  #_
  (db/with-tx
    (db/sync-subs-messages {:user_id user_id})
    (db/sync-subs-counters {:user_id user_id})
    (db/sync-user-counters {:user_id user_id})))

(defn sync-user-safe
  ;; todo wrap with log etc
  [user]
  (sync-user (:id user)))

;;
;; Dev import
;;

(defn feed-import
  []
  (with-open [rdr (clojure.java.io/reader "rss.txt")]
    (doseq [url (line-seq rdr)]
      (println url)
      (sync-feed-by-url url))))
