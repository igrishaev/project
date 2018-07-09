(ns project.spec
  (:require [project.error :as e]
            project.spec.env
            project.spec.email
            [project.spec.util :refer
             [invalid
              ->url
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
(s/def ::title not-empty-string)

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
