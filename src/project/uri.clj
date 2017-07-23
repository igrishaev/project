(ns project.uri
  (:import java.net.URI))

(defn read-uri [value]
  (java.net.URI. value))
