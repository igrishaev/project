(ns project.models
  (:require [project.db :as db]
            [project.url :as url]))

(def upsert-user
  (partial db/upsert! :users "(email)"))

(def upsert-feed
  (partial db/upsert! :feeds "(url_source)"))

(def upsert-entry
  (partial db/upsert! :entries "(feed_id, guid)"))

(def upsert-subs
  (partial db/upsert! :subs "(feed_id, user_id)"))

(def upsert-message
  (partial db/upsert! :messages "(sub_id, entry_id)"))

;;
;; Common
;;

(defn model-exists?
  [table params]
  (db/query-hf
   {:select [:id]
    :from [:subs]
    :where (cons :and (for [[k v] params] [:= k v]))
    :limit 1}))

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
     (upsert-user
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
     (upsert-user
      {:email email
       :source "email"}))))

;;
;; Subs
;;

;; todo bump subs count

(def sub-exists? (partial model-exists? :subs))

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
    (db/query-hf
     {:insert-into :subs
      :values [values]
      :returning [:*]})))

;; todo dec subs count
;; todo mb not now?

;; todo delete messages?
;; or do that later?

(defn unsubscribe
  [user sub_id]
  (db/execute-h
   {:delete-from :subs
    :where [:and
            [:= :id sub_id]
            [:= :user_id (:id user)]]}))

(def subs-query
  {:select
   [:f.*
    (db/raw "row_to_json(s) as sub")]
   :from [[:feeds :f] [:subs :s]]
   :where [:and
           [:= :s.feed_id :f.id]]})

(defn get-user-subs
  [user]
  (db/query-h
   (-> subs-query
       (update :where conj [:= :s.user_id (:id user)])
       (assoc :order [[:s.id :desc]]))))

(defn get-user-sub
  [user sub]
  (db/query-hf
   (-> subs-query
       (update :where conj [:= :s.user_id (:id user)])
       (update :where conj [:= :s.id (:id sub)])
       (assoc :limit 1))))

;;
;; Messages
;;

(defn message
  [user entry & [params]]
  (upsert-message
   (merge
    params
    {:user_id (:id user)
     :entry_id (:id entry)})))


(def messages-query
  {:select
   [(db/raw "row_to_json(e) as entry")
    (db/raw "row_to_json(m) as message")]
   :from [[:entries :e] [:messages :m]]
   :where [:and
           [:= :m.entry_id :e.id]
           [:not :m.is_read]]
   :order-by [[:m.id :desc]]})

(defn get-messages
  [sub & [from_id]]
  (db/query-h
   (cond-> messages-query
     true
     (update :where conj [:= :m.sub_id (:id sub)])
     true
     (assoc :limit 10)
     from_id
     (update :where conj [:< :m.id from_id]))))

;; todo bump sub read counter
;; todo make batch?

(defn mark-read
  [message_id sub_id is_read]
  (db/execute-h
   {:update :messages
    :set {:is_read is_read
          :date_read (when is_read :%now)
          :updated_at :%now}
    :where [:and
            [:= :id message_id]
            [:= :sub_id sub_id]]}))

;;
;; Feed
;;

(def find-feed (partial db/find-first :feeds))

(defn get-feed-by-id
  [id]
  (find-feed {:id id}))

(defn get-feed-by-url
  [url]
  (find-feed {:url_source url}))

(defn update-feed
  [id params]
  (db/query-h
   {:update :feeds
    :set (assoc params :updated_at :%now)
    :where [:= :id id]
    :returning [:*]}))

(defn create-feed
  [url]
  (let [url_host (url/get-host url)
        url_favicon (url/get-fav-url url)
        params {:url_source url
                :url_host url_host
                :url_favicon url_favicon}]
    (db/query-h
     {:insert-into :feeds
      :values [params]
      :returning [:*]})))

;; todo upsert?

(defn get-or-create-feed
  [url]
  (db/with-tx
    (if-let [feed (get-feed-by-url url)]
      feed
      (create-feed url))))
