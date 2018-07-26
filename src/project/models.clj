(ns project.models
  (:require [project.db :as db]
            [project.url :as url]))

;;
;; Cleaning
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
   :gender
   :sync_date_last])


(def clean-feed (partial clean-model feed-fields))

(def clean-user (partial clean-model user-fields))


;;
;; Upsert
;;


(def upsert-user
  (partial db/upsert! :users "(email)"))

(let [constraint "(url_source)"]

  (def upsert-feed
    (partial db/upsert! :feeds constraint))

  (def upsert-feeds
    (partial db/upsert-multi! :feeds constraint)))

(def upsert-entries
  (partial db/upsert-multi! :entries "(feed_id, guid)"))

(let [constraint "(feed_id, user_id)"]

  (def upsert-sub
    (partial db/upsert! :subs constraint))

  (def upsert-subs
    (partial db/upsert-multi! :subs constraint)))

(def upsert-message
  (partial db/upsert! :messages "(user_id, entry_id)"))


;;
;; User
;;


;; todo email lowercase

(defn get-user-by-id
  [id]
  (db/get-by-id :users id))


(defn upsert-google-user
  [auth params]
  (let [{:keys [id
                name
                link
                email
                gender
                locale
                picture]} params]

    (upsert-user
     {:email email
      :source "google"
      :source_id id
      :name name
      :source_url link
      :locale locale
      :avatar_url picture
      :gender gender
      :auth_data auth})))


(defn upsert-email-user
  [params]
  (let [{:keys [email]} params]
    (upsert-user
     {:email email
      :source "email"})))

;;
;; Subs
;;

(def find-sub (partial db/find-first :subs))

(defn get-sub-title
  [params feed]
  (or (:title params)
      (:title feed)
      (:subtitle feed)
      (:url_source feed)))

;;
;; Messages
;;

(def find-message (partial db/find-first :messages))

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

;;
;; Entries
;;

(def find-entry (partial db/find-first :entries))

(defn get-entry-by-id
  [id]
  (db/get-by-id :entries id))
