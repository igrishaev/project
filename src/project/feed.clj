(ns project.feed
  "Common feed parser."
  (:require [project.xml :as xml]
            [project.http :as http]
            [project.rss :as rss]
            [project.atom :as atom]
            [project.db :as db]
            [project.uri :as uri]
            [project.proto :as proto]
            [clj-time.core :as time]
            [clj-time.format :as format])
  (:import java.net.URI ;; todo
           project.rss.RSSFeed
           project.atom.AtomFeed))

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

      (and (= tag :feed)
           (= (-> node :attrs :xmlns)
              "http://www.w3.org/2005/Atom"))
      (atom/parse node)

      :else
      (-> "wrong XML tag: %s"
          (format tag)
          Exception.
          throw))))

(defmacro safe [& body] ;; todo move
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

(defn parse-payload [content-type payload]
  (let [feed (case content-type
               ("text/xml; charset=utf-8"
                "application/xml"
                "application/rss+xml; charset=utf-8"
                "application/atom+xml; charset=UTF-8"))]
    (parse-xml payload)))

(defn fetch-feed [url]
  (let [response (http/get url)
        payload  (:body response)
        ctype    (-> response :headers (get "Content-Type"))]
    (parse-payload ctype payload)))

(defn save-feed [url feed]

  (db/transact [{:feed/url-source (uri/read-uri url)
                 }])

  )

;; https://lenta.ru/rss
;; https://habrahabr.ru/rss/best/
;; https://meduza.io/rss/all

;; http://blog.case.edu/news/feed.atom
;; https://golem.ph.utexas.edu/~distler/blog/atom10.xml

(defn get-feed [url]
  (let [feed (-> url
                 http/get
                 :body
                 xml/parse
                 RSSFeed.
                 ;; AtomFeed.
                 )]

    #_{:feed/lang (proto/get-feed-lang feed)
     :feed/title (proto/get-feed-title feed)
     :feed/pub-date (proto/get-feed-pub-date feed)
     :feed/description (proto/get-feed-description feed)
     :feed/tags (proto/get-feed-tags feed)
     :feed/image (proto/get-feed-image feed)
     :feed/icon (proto/get-feed-icon feed)}

    (for [entity (proto/get-feed-entities feed)]
      {:entity/title (proto/get-entity-title entity)
       :entity/link (proto/get-entity-link entity)
       ;; :entity/description (proto/get-entity-description entity)
       :entity/guid (proto/get-entity-guid entity)
       :entity/author (proto/get-entity-author entity)
       :entity/pub-date (proto/get-entity-pub-date entity)

       :entity/tags
       (for [tag (proto/get-entity-tags entity)]
         (proto/get-tag-name tag))

       :entity/media
       (for [media (proto/get-entity-media entity)]
         {:media/url (proto/get-media-url media)
          :media/type (proto/get-media-type media)
          :media/size (proto/get-media-size media)})})))
