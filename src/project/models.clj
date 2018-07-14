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
  (partial db/upsert! :messages "(user_id, entry_id)"))

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

(def sub-exists? (partial db/find-first :subs))

(defn get-sub-by-id
  [id]
  (db/get-by-id :subs id))

;; todo delete
(defn get-sub-by-user-and-id
  [user id]
  (db/find-first
   :subs {:id id :user_id (:id user)}))

(defn get-sub-title
  [params feed]
  (or (:title params)
      (:title feed)
      (:subtitle feed)
      (:url_source feed)))

;; todo delete?

(defn subscribed?
  [user feed]
  (db/find-first
   :subs
   {:user_id (:id user)
    :feed_id (:id feed)}))

(defn has-sub?
  [user sub_id]
  (db/find-first
   :subs
   {:id sub_id
    :user_id (:id user)}))

(defn subscribe
  [user feed & [params]]
  (let [title (get-sub-title params feed)
        values {:user_id (:id user)
                :feed_id (:id feed)
                :title title}]
    (db/insert! :subs values)))

;; todo dec subs count
;; todo mb not now?

;; todo delete messages?
;; or do that later?

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

;; todo bump sub read counter
;; todo make batch?


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
  (db/update! :feeds params ["id = ?" id]))

(defn create-feed
  [url]
  (let [url_host (url/get-host url)
        url_favicon (url/get-fav-url url)
        params {:url_source url
                :url_host url_host
                :url_favicon url_favicon}]
    (db/insert! :feeds params)))

;; todo upsert?

(defn get-or-create-feed
  [url]
  (db/with-tx
    (if-let [feed (get-feed-by-url url)]
      feed
      (create-feed url))))
