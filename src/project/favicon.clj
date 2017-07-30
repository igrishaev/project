(ns project.favicon
  (:require [project.uri :as uri]))

(def google-template "https://www.google.com/s2/favicons?domain=%s")

(defn discover [link]
  (when-let [domain (uri/get-domain link)]
    (format google-template domain)))
