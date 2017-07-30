(ns project.uri
  (:require [project.utils :refer [safe]])
  (:import java.net.URI))

(defn reader [value]
  (URI. value))

(defn uri? [value]
  (instance? URI value))

(defn parse [link]
  (safe
   (let [uri (URI. link)
         scheme (.getScheme uri)
         host (.getHost uri)
         path (.getPath uri)]
     {:scheme scheme
      :host host
      :path path})))

(defn get-domain [link]
  (some-> link
          parse
          :host))
