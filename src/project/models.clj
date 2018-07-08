(ns project.models
  (:require
   [project.db :as db]))

(defn subscribe
  [user feed & [params]]
  (let [title (or (:title params)
                  (:title feed)
                  (:subtitle feed)
                  (:url_source feed))]
    (db/upsert-subs
     (merge
      params
      {:user_id (:id user)
       :feed_id (:id feed)
       :title title}))))

(defn unsubscribe
  [user_id sub_id]
  (db/execute!
   (db/format
    {:update :subs
     :set {:deleted true}
     :where [:and
             [:= :id sub_id]
             [:= :user_id user_id]
             [:not :deleted]]})))

(defn message
  [user entry & [params]]
  (db/upsert-message
     (merge
      params
      {:user_id (:id user)
       :entry_id (:id entry)})))

(defn update-feed
  [id params]
  (db/execute!
   (db/format
    {:update :feeds
     :set (assoc params :updated_at :%now)
     :where [:= :id id]})))

(defn get-feed-by-id
  [id]
  (first
   (db/find-by-keys
    :feeds {:id id :deleted false})))

(defn get-feed-by-url
  [url]
  (first
   (db/find-by-keys
    :feeds {:url_source url :deleted false})))

(defn create-feed
  [url]
  (first
   (db/query
    (db/format
     {:insert-into :feeds
      :values [{:url_source url}]
      :returning [:*]}))))

(defn get-or-create-feed
  [url]
  (db/with-tx
    (if-let [feed (get-feed-by-url url)]
      feed
      (create-feed url))))

(defn get-sub-by-id
  [id]
  (first
   (db/find-by-keys
    :subs {:id id :deleted false})))

(defn get-user-subs
  [user_id]
  (db/query
   (db/format
    {:select [:f.*
              [:s.title :sub_title]]
     :from [[:feeds :f] [:subs :s]]
     :where [:and
             [:= :s.user_id user_id]
             [:= :s.feed_id :f.id]
             [:not :f.deleted]
             [:not :s.deleted]]
     :limit 100})))

(defn get-messages
  [sub_id]
  (db/query
   (db/format
    {:select [:e.*]
     :from [[:messages :m] [:entries :e]]
     :where [:and
             [:= :m.sub_id sub_id]
             [:= :m.entry_id :e.id]
             [:not :m.is_read]
             [:not :m.deleted]
             [:not :e.deleted]]
     :order-by [[:e.id :desc]]
     :limit 100})))

(defn mark-read
  [user msg-ids]
  (db/with-tx

    (db/execute!
     (db/format
      {:update :messages
       :set {:is_read true
             :date_read :%now
             :updated_at :%now}
       :where [:and
               [:= :user_id (:id user)]
               [:in :id msg-ids]
               [:not :deleted]]}))

;; todo update unread count



    ))

(defn mark-unread
  [user msg-ids]

  ;; todo update unread count

  (db/execute!
   (db/format
    {:update :messages
     :set {:is_read true
           :date_read :nil
           :updated_at :%now}
     :where [:and
             [:= :user_id (:id user)]
             [:in :id msg-ids]
             [:not :deleted]]})))
