(ns ui.time
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]))

(def formatter-iso
  (:date-time f/formatters))

(defn parse-iso
  [iso]
  (f/parse formatter-iso iso))

;; todo make it better

(defn humanize
  [iso]
  (let [dt (parse-iso iso)]
    (f/unparse (:year-month-day f/formatters) dt)))
