(ns project.time
  "Misc time wrappers."
  (:require [clj-time.core :as t]
            [clj-time.format :as f]
            [cheshire.generate :refer [add-encoder]])
  (:import org.joda.time.DateTime))

(def ^:private
  iso8601 (f/formatters :date-time))

(add-encoder
 DateTime
 (fn [dt jsonGenerator]
   (.writeString jsonGenerator (f/unparse iso8601 dt))))

(defn epoch
  "UNIX timestamp in seconds as integer."
  []
  (quot (System/currentTimeMillis) 1000))

(defn ahead [secs]
  (t/plus (t/now) (t/seconds secs)))

(def now t/now)

(def formatter-iso
  (:date-hour-minute-second f/formatters))

(defn parse-iso
  [iso]
  (f/parse formatter-iso iso))

(defn parse-iso-safe
  [iso]
  (when iso
    (try
      (parse-iso iso)
      (catch Exception e))))

(defn parse-iso-now
  [iso]
  (or (parse-iso-safe iso)
      (t/now)))
