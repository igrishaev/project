(ns project.spec
  (:require [project.error :as e]
            project.spec.env
            project.spec.email
            [project.spec.util :refer
             [invalid
              ->int
              ->keyword
              ->not-empty-string
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
(s/def ::feed_id foreign-key)
(s/def ::message_id foreign-key)
(s/def ::last_id (s/nilable foreign-key))
(s/def ::title ->not-empty-string)
(s/def ::is_read boolean?)

;;
;; Api
;;

(s/def ::api.base
  (s/keys :req-un [::action]))

;;
;; Search
;;

(s/def ::term ->not-empty-string)

(s/def ::api.search-feeds
  (s/keys :req-un [::term]))

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

(defn zpos?
  [n]
  (>= n 0))

(s/def ::offset
  (s/and int? zpos?))

(s/def ::api.messages
  (s/keys :req-un [::feed_id]
          :opt-un [::offset]))

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

(s/def ::ordering
  #{"new_first" "old_first"})

(s/def ::unread_only boolean?)

(s/def ::api.update-subscription
  (s/keys :opt-un [::layout
                   ::auto_read
                   ::ordering
                   ::unread_only]))

;;
;; Logout
;;

(s/def ::api.logout
  (s/keys :req-un []))
