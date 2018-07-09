(ns project.spec.util
  (:require [clojure.spec.alpha :as s]))

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
