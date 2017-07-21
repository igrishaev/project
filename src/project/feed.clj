(ns project.feed
  "Common feed parser."
  (:require [project.xml :as xml]
            [project.http :as http]
            [project.rss :as rss]
            [clj-time.core :as time]
            [clj-time.format :as format]
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
    ("text/xml; charset=utf-8"
     "application/xml"
     "application/rss+xml; charset=utf-8")
    (parse-xml payload)))

(defmacro safe [& body]
  `(try
     ~@body
     (catch Exception e#
       nil)))

(defn to-str [val]
  (if (string? val)
    val
    (str val)))

(defn get-title [feed]
  (to-str
   (or (-> :title feed)
       (safe
        (some-> feed :link URI. .getHost)))))

(defn get-description [feed]
  (to-str
   (or (-> feed :description))))

(defn get-url-site [feed]
  (to-str
   (safe
    (some-> feed :link URI.))))

(defn get-body-html [item]
  (to-str
   (-> item :description)))

(defn get-body-text [item]
  (to-str
   (-> item :description)))

(defn get-guid [item]
  (to-str
   (or (:guid item)
       (:link item)
       (:pubDate item)
       (time/now))))

(defn get-item-link [item]
  (to-str
   (or
    (safe
     (some-> item :link URI.)))))

(def rfc822
  (format/with-locale
    (format/formatter
     time/utc
     "EEE, dd MMM yyyy HH:mm:ss Z"
     "EEE, dd MMM yyyy HH:mm:ss z")
    java.util.Locale/US))

(defn parse-rfc822 [val]
  (format/parse rfc822 val))

(defn get-item-pub-date [item]
  (or
   (safe
    (some-> item :pubDate parse-rfc822))
   (time/now)))

(defn normalize-item [item]
  {:title (get-title item)
   :body_html (get-body-html item)
   :body_text (get-body-text item)
   :guid (get-guid item)
   :url_link (get-item-link item)
   :date_published_at (get-item-pub-date item)})

(defn normalize-feed [feed]
  {:title (get-title feed)
   :description (get-description feed) ;; clear html
   :url_site (get-url-site feed)
   ;; :url_favicon ??
   ;; :url_banner ??
   :messages (map normalize-item (:items feed))})

(defn parse-payload [content-type payload]
  (let [feed (parse-payload-cond content-type payload)]
    (normalize-feed feed)))

(defn fetch-feed [source]
  (let [url      (:url_src source)
        response (http/get url)
        payload  (:body response)
        ctype    (-> response :headers (get "Content-Type"))]
    (parse-payload ctype payload)))
