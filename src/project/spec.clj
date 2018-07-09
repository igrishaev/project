(ns project.spec
  (:require [project.error :as e]
            project.spec.env
            project.spec.email
            [project.spec.util :refer
             [invalid ->url ->keyword]]

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
;; Api
;;

(s/def ::action ->keyword)

(s/def ::api.base
  (s/keys :req-un [::action]))

;;
;; Preview
;;

(s/def ::url ->url)

(s/def ::api.preview
  (s/keys :req-un [::url]))
