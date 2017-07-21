(ns project.feed
  "Common feed parser."
  (:require [project.xml :as xml]
            [project.http :as http]
            [project.rss :as rss]
            [clojure.java.io :as io])
  (:import java.net.URI))


(defn discover-favicon [feed]
  (let [url (-> feed :link URI.)
        schema (.getScheme url)
        host (.getHost url)
        path "/favicon.ico"
        frag ""
        url-fav (str (URI. schema host path frag))
        _ (http/head url-fav)]
    url-fav))


(defn get-main-fields [feed]
  {:title "feed"})


(defn get-item-detail [entity]
  {:title "item"})


(defn get-item-fields [feed]
  (map get-item-detail (:entities feed)))


(defn parse-xml [payload]
  (let [node (xml/parse payload)
        tag (:tag node)]
    (cond
      (= tag :rss)
      (rss/parse node)

      :else
      (-> "wrong XML tag: %s"
          (format tag)
          Exception.
          throw))))


(defn parse-payload-cond [content-type payload]
  (case content-type
    ("application/xml" "application/rss+xml; charset=utf-8")
    (parse-xml payload)))


(defn parse-payload [content-type payload]
  (let [feed (parse-payload-cond content-type payload)
        fav-url (discover-favicon feed)]
    (assoc feed :url_favicon fav-url)))
