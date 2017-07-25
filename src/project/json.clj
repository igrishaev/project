(ns project.json
  (:require [cheshire.generate
             :refer [add-encoder encode-str]])
  (:import org.joda.time.DateTime
           [java.net URL URI]))

(add-encoder URL encode-str)
(add-encoder URI encode-str)
(add-encoder DateTime encode-str)
