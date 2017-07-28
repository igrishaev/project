(ns project.feed
  "Common feed parser."
  (:require [project.xml :as xml]
            [project.http :as http]
            [project.rss :as rss]
            [project.atom :as atom]
            [project.db :as db]
            [project.uri :as uri]
            [project.raise :refer [raise]]
            [project.proto :as proto]
            [clj-time.core :as time]
            [clj-time.format :as format])
  (:import java.net.URI ;; todo
           project.rss.RSSFeed
           org.joda.time.DateTime
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

;; (defn parse-xml [payload]
;;   (let [node (xml/parse payload)
;;         tag (:tag node)]
;;     (cond
;;       (= tag :rss)
;;       (rss/parse node)

;;       (and (= tag :feed)
;;            (= (-> node :attrs :xmlns)
;;               "http://www.w3.org/2005/Atom"))
;;       (atom/parse node)

;;       :else
;;       (-> "wrong XML tag: %s"
;;           (format tag)
;;           Exception.
;;           throw))))

(defmacro safe [& body] ;; todo move
  `(try
     ~@body
     (catch Exception e#
       nil)))

(defn to-str [val]
  (if (string? val)
    val
    (str val)))



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
       (time/now)))) ;; guid
;; (-> (java.util.UUID/randomUUID) str)


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

;; (defn parse-payload [content-type payload]
;;   (let [feed (case content-type
;;                ("text/xml; charset=utf-8"
;;                 "application/xml"
;;                 "application/rss+xml; charset=utf-8"
;;                 "application/atom+xml; charset=UTF-8"))]
;;     (parse-xml payload)))

;; (defn )

(defn parse-xml
  [payload]
  (-> payload
      xml/parse
      RSSFeed.))

(defn parse-atom
  [payload]
  (-> payload
      xml/parse
      AtomFeed.))

(defn parse-json
  [payload]
  #_(-> payload
      json/parse
      JsonFeed.))

(defn parse-feed
  [feed-type payload]
  (case feed-type
    :feed/rss (parse-xml payload)
    :feed/atom (parse-atom payload)
    :feed/json (parse-json payload)))

#_(defn get-title [feed]
  (to-str
   (or (-> :title feed)
       (safe
        (some-> feed :link URI. .getHost)))))

(defn coerce-title [val]
  (if (instance? DateTime val)
    val
    (or
     (safe
      (parse-rfc822 val))
     (time/now))))

(defn coerce-date [val]
  (if (instance? DateTime val)
    val
    (or
     (safe
      (parse-rfc822 val))
     (time/now))))

(defn normalize-feed
  [feed]
  {:title (proto/get-feed-title feed)
   :language (proto/get-feed-lang feed)
   :description (proto/get-feed-description feed)
   :url_site (proto/get-feed-link feed)
   ;; :url_source
   :url_favicon (proto/get-feed-icon feed)
   :url_image (proto/get-feed-image feed)

   :date_published (-> feed
                       proto/get-feed-pub-date
                       coerce-date)

   :tags (for [tag (proto/get-feed-tags feed)]
           (proto/get-tag-name tag))

   :items
   (for [entity (proto/get-feed-entities feed)]
     {:title (proto/get-entity-title entity)

      :link (proto/get-entity-link entity)

      :description (proto/get-entity-description entity)

      :guid (proto/get-entity-guid entity)

      :author (-> entity
                  proto/get-entity-author)

      :date_published (-> entity
                          proto/get-entity-pub-date
                          coerce-date)

      :tags (for [tag (proto/get-entity-tags entity)]
              (proto/get-tag-name tag))

      :media (for [media (proto/get-entity-media entity)]
               {:url (proto/get-media-url media)
                :type (proto/get-media-type media)
                :size (proto/get-media-size media)})})})


(defn guess-feed-type
  [url response]
  (let [content-type (-> response
                         :headers
                         (get "Content-Type")
                         (or ""))]

    )
  ;;
  :feed/rss
  ;; :feed/atom
  ;; :feed/json

  ;; (raise "unknown feed type")
)

;; https://lenta.ru/rss
;; https://habrahabr.ru/rss/best/
;; https://meduza.io/rss/all

;; http://blog.case.edu/news/feed.atom
;; https://golem.ph.utexas.edu/~distler/blog/atom10.xml


(defn fetch-feed [url]
  (let [response (http/get url)
        payload (:body response)
        feed-type (guess-feed-type url response)
        feed (parse-feed feed-type payload)
        data (normalize-feed feed)]

    data

    ))


#_(defn save-feed [url data]
  (db/with-trx
    (let [src-params {:title (get-title data)
                      ;; :url_site

                      }


          src (if (db/source-exists? {:url url})
            1
            2)

          src (first (db/insert! :sources src-params))
          items (:items data)]
      #_(doseq [item items]
        (let [msg-params {}
              msg (first (db/insert! :messages msg-params))]
          msg)))))
