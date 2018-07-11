(ns ui.util
  (:refer-clojure :exclude [uuid])
  (:require [goog.string :as gstring]
            [goog.string.format]
            [reagent.dom.server :as dom]

            [clojure.string :as str]))

(def clear-str (comp not-empty str/trim))

(def format gstring/format)

(defn uuid []
  (str (random-uuid)))

(def default-max-len 50)

(defn trim-string
  [string & [max-len]]
  (let [max-len (or max-len default-max-len)
        string (str/trim string)]
    (if (> (count string) max-len)
      (str (subs string 0 max-len) "...")
      string)))
