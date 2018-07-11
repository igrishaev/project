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
(s/def ::sub_id foreign-key)
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

(s/def ::api.preview
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
  (s/keys :req-un [::sub_id]))

;;
;; Subscriptions
;;

(s/def ::api.subscriptions
  (s/keys :req-un []))

;;
;; Messages
;;

(s/def ::api.messages
  (s/keys :opt-un [::from_id
                   ::sub_id]))

;;
;; Mark read
;;

(s/def ::api.mark-read
  (s/keys :req-un [::sub_id
                   ::message_id
                   ::is_read]))
