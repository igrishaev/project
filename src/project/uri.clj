(ns project.uri
  (:import java.net.URI))

(defn read-uri [value]
  (URI. value))

(defn is-uri? [value]
  (instance? URI value))
