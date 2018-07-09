(ns project.spec.util
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as str])
  (:import java.net.URL))

(def invalid :clojure.spec.alpha/invalid)

(defmacro with-ivalid
  [& body]
  `(try
     ~@body
     (catch Throwable e#
       invalid)))

(def ->int
  (s/conformer
   (fn [x]
     (cond

       (int? x) x

       (string? x)
       (with-ivalid
         (Integer/parseInt x))

       :else invalid))))

(def ->url
  (s/conformer
   (fn [x]
     (cond

       (string? x)
       (with-ivalid
         (-> x URL. .toString))

       :else invalid))))

(def ->keyword
  (s/conformer
   (fn [x]
     (cond
       (keyword? x) x
       (string? x) (keyword x)
       :else invalid))))

(def re-email? (partial re-matches #".+?@.+?\..+?"))

(def ->email
  (s/and
   string?
   (s/conformer str/trim)
   (s/conformer str/lower-case)
   not-empty
   re-email?))
