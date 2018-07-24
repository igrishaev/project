(ns ui.time
  (:require [cljs-time.core :as t]
            [cljs-time.format :as f]

            [cljsjs.timeago :as ago]))


(def _timeago (js/timeago))


(def formatter-iso
  (:date-time f/formatters))


(defn parse-iso
  [iso]
  (f/parse formatter-iso iso))


(defn humanize
  [iso]
  (.format _timeago iso))
