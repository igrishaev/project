(ns project.spec
  (:require [project.error :as e]
            [clojure.spec.alpha :as s]
            [clojure.string :as str]
            #_
            [clojure.core.match :refer [match]]))

;;
;; Helpers
;;

(def invalid :clojure.spec.alpha/invalid)

(defmacro with-ivalid
  [& body]
  `(try
     ~@body
     (catch Throwable e#
       invalid)))

(defn conform [spec value]
  (let [result (s/conform spec value)]
    (when-not (= result invalid)
      result)))

(def valid? s/valid?)

(defn url? [x]
  (when (string? x)
    (e/with-catch
      (java.net.URL. x)
      true)))

(defn ->url [x]
  (with-ivalid
    (java.net.URL. x)))

(s/def ::url
  (s/conformer ->url))

(defn ->int [x]
  (cond
    (int? x) x
    (string? x)
    (with-ivalid
      (Integer/parseInt x))
    :else invalid))
