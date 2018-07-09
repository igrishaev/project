(ns project.models
  (:require [project.db :as db]))

;;
;; User
;;

;; todo email lowercase

(defn get-user-by-id
  [id]
  (db/get-by-id :users id))

;; todo return a full user

(defn upsert-google-user
  [auth params]
  (let [{:keys [id
                name
                link
                email
                gender
                locale
                picture]} params]

    (first
     (db/upsert-user
      {:email email
       :source "google"
       :source_id id
       :name name
       :source_url link
       :locale locale
       :avatar_url picture
       :gender gender
       :auth_data auth}))))

;; todo return a full user

(defn upsert-email-user
  [params]
  (let [{:keys [email]} params]
    (first
     (db/upsert-user
      {:email email
       :source "email"}))))

;;
;; Subs
;;

;; todo bump subs count

(defn get-sub-by-id
  [id]
  (db/get-by-id :subs id))

(defn get-sub-by-user-and-id
  [user id]
  (first
   (db/find-by-keys
    :subs {:id id :user_id (:id user)})))

(defn get-sub-title
  [params feed]
  (or (:title params)
      (:title feed)
      (:subtitle feed)
      (:url_source feed)))

(defn subscribed?
  [user feed]
  (first
   (db/find-by-keys
    :subs {:user_id (:id user)
           :feed_id (:id feed)})))

(defn has-sub?
  [user sub_id]
  (first
   (db/find-by-keys
    :subs {:id sub_id
           :user_id (:id user)})))

(defn subscribe
  [user feed & [params]]
  (let [title (get-sub-title params feed)
        values {:user_id (:id user)
                :feed_id (:id feed)
                :title title}]
    (first
     (db/query
      (db/format
       {:insert-into :subs
        :values [values]
        :returning [:*]})))))

;; todo dec subs count
;; todo mb not now?

;; todo delete messages?
;; or do that later?

(defn unsubscribe
  [user sub_id]
  (db/execute!
   (db/format
    {:delete-from :subs
     :where [:and
             [:= :id sub_id]
             [:= :user_id (:id user)]]})))

(def subs-query
  {:select
   [(db/raw "row_to_json(f) as feed")
    (db/raw "row_to_json(s) as sub")]
   :from [[:feeds :f] [:subs :s]]
   :where [:and
           [:= :s.feed_id :f.id]]})

(defn get-user-subs
  [user]
  (db/query
   (db/format
    (-> subs-query
        (update :where conj [:= :s.user_id (:id user)])
        (assoc :order [[:s.id :desc]])))))

(defn get-user-sub
  [user sub]
  (first
   (db/query
    (db/format
     (-> subs-query
         (update :where conj [:= :s.user_id (:id user)])
         (update :where conj [:= :s.id (:id sub)])
         (assoc :limit 1))))))

;;
;; Messages
;;

(def messages-query
  {:select
   [(db/raw "row_to_json(e) as entry")
    (db/raw "row_to_json(m) as message")]
   :from [[:entries :e] [:messages :m]]
   :where [:and
           [:= :m.entry_id :e.id]]
   :order-by [[:m.id :desc]]})

(defn get-messages
  [sub & [from_id]]
  (db/query
   (db/format
    (cond-> messages-query

      true
      (update :where conj [:= :m.sub_id (:id sub)])

      true
      (assoc :limit 10)

      from_id
      (update :where conj [:< :m.id from_id])))))

;;
;; Other
;;


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
    :feeds {:id id})))

(defn get-feed-by-url
  [url]
  (first
   (db/find-by-keys
    :feeds {:url_source url})))

;; todo fill host, favicon etc

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
               [:in :id msg-ids]]}))

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
             [:in :id msg-ids]]})))
