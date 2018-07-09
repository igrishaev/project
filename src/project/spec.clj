(ns project.spec
  (:require [project.error :as e]
            project.spec.env
            project.spec.email

            [clojure.spec.alpha :as s]))

;;
;; Helpers
;;

(def invalid :clojure.spec.alpha/invalid)

(defn conform [spec value]
  (let [result (s/conform spec value)]
    (when-not (= result invalid)
      result)))

(def explain-str s/explain-str)

(def valid? s/valid?)

;;
;; Api
;;

(s/def ::action
  (s/conformer
   (fn [x]
     (if (string? x)
       (keyword x)
       invalid))))

(s/def ::api.base
  (s/keys :req-un [::action]))

;;
;; Demo
;;

(s/def ::foo
  (s/conformer
   (fn [x]
     (if (string? x)
       (keyword x)
       invalid))))

(s/def ::api.demo
  (s/keys :req-un [::foo]))

#_
(defmacro with-ivalid
  [& body]
  `(try
     ~@body
     (catch Throwable e#
       invalid)))

#_
(defn ->url [x]
  (with-ivalid
    (java.net.URL. x)))

#_
(s/def ::url
  (s/conformer ->url))

#_
(defn ->int [x]
  (cond
    (int? x) x
    (string? x)
    (with-ivalid
      (Integer/parseInt x))
    :else invalid))
