(ns project.spec
  (:require [project.error :as e]
            project.spec.env
            project.spec.email
            [project.spec.util :refer
             [invalid
              ->url
              ->int
              ->keyword
              not-empty-string
              foreign-key]]

            [clojure.spec.alpha :as s]))

;;
;; Helpers
;;

(defn conform [spec value]
  (let [result (s/conform spec value)]
    (when-not (= result invalid)
      result)))

(def explain-str s/explain-str)

(def valid? s/valid?)

;;
;; Fields
;;

(s/def ::action ->keyword)
(s/def ::url ->url)
(s/def ::feed_id foreign-key)
(s/def ::message_id foreign-key)
(s/def ::from_id (s/nilable foreign-key))
(s/def ::title not-empty-string)
(s/def ::is_read boolean?)

;;
;; Api
;;

(s/def ::api.base
  (s/keys :req-un [::action]))

;;
;; Preview
;;

(s/def ::api.search-feeds
  (s/keys :req-un [::url]))

;;
;; Subscribe
;;

(s/def ::api.subscribe
  (s/keys :req-un [::feed_id]
          :opt-in [::title]))

;;
;; Unsubscribe
;;

(s/def ::api.unsubscribe
  (s/keys :req-un [::feed_id]))

;;
;; Subscriptions
;;

(s/def ::api.subscriptions
  (s/keys :req-un []))

;;
;; Messages
;;

(s/def ::api.messages
  (s/keys :opt-un [::feed_id
                   ::from_id]))

;;
;; Mark read
;;

(s/def ::api.mark-read
  (s/keys :req-un [::entry_id
                   ::is_read]))

;;
;; User info
;;

(s/def ::api.user-info
  (s/keys :req-un []))


;;
;; Update subscription
;;

(s/def ::auto_read boolean?)

(s/def ::layout
  #{"full_article" "titles_only" "cards"})

(s/def ::api.update-subscription
  (s/keys :opt-un [::layout
                   ::auto_read]))
