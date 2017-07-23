(ns project.uri
  (:import java.net.URI))

(defn make-uri [value]
  (java.net.URI. value))

(defn read-uri [value]
  (make-uri value))
