(ns project.rome
  (:require [project.http :as http])
  (:import java.net.URL

           [org.jdom2
            Element
            Attribute]

           [com.rometools.rome.feed.synd
            SyndFeed
            SyndEntry
            SyndImage
            SyndCategory
            SyndPerson
            SyndLink
            SyndContent
            SyndEnclosure]

           [com.rometools.rome.io
            SyndFeedInput
            XmlReader]))


(defprotocol ToClojure
  (->clj [obj]))


(defn parse-internal
  [source]
  (let [reader (new XmlReader source)
        input (new SyndFeedInput)
        feed (.build input reader)]
    (->clj feed)))


;; TODO pass encoding
;; TODO pass content type


(defn parse-http-resp
  [http-resp]
  (parse-internal (:body http-resp)))


(defn parse-url
  [url]
  (let [resp (http/get url {:as :stream})]
    (parse-http-resp resp)))


(extend-type SyndContent
  ToClojure
  (->clj [c]
    {:type (.getType c)
     :mode (.getMode c)
     :value (.getValue c)}))


(extend-type SyndEnclosure
  ToClojure
  (->clj [e]
    {:url (.getUrl e)
     :length (.getLength e)
     :type (.getType e)}))


(extend-type SyndLink
  ToClojure
  (->clj [l]
    {:rel (.getRel l)
     :type (.getType l)
     :href (.getHref l)
     :title (.getTitle l)
     :href-lang  (.getHreflang l)
     :length (.getLength l)}))


(extend-type SyndPerson
  ToClojure
  (->clj [p]
    {:name (.getName p)
     :uri (.getUri p)
     :email (.getEmail p)}))


(extend-type SyndCategory
  ToClojure
  (->clj [c]
    {:name (.getName c)
     :taxonomy-url (.getTaxonomyUri c)}))


(extend-type SyndImage
  ToClojure
  (->clj [i]
    {:title (.getTitle i)
     :url (.getUrl i)
     :width (.getWidth i)
     :height (.getHeight i)
     :link (.getLink i)
     :description (.getDescription i)}))


(extend-type SyndEntry
  ToClojure
  (->clj [e]
    {:authors        (map ->clj (seq (.getAuthors e)))
     :categories     (map ->clj (seq (.getCategories e)))
     :contents       (map ->clj (seq (.getContents e)))
     :contributors   (map ->clj (seq (.getContributors e)))
     :enclosures     (map ->clj (seq (.getEnclosures e)))
     :description    (when-let [d (.getDescription e)]
                       (->clj d))
     :author         (.getAuthor e)
     :link           (.getLink e)
     :published-date (.getPublishedDate e)
     :title          (.getTitle e)
     :updated-date   (.getUpdatedDate e)
     :uri            (.getUri e)
     :comments       (.getComments e)

     :foreign-markup (map ->clj (.getForeignMarkup e))}))


(extend-type SyndFeed
  ToClojure
  (->clj [f]
    {:authors        (map ->clj (seq (.getAuthors f)))
     :categories     (map ->clj (seq (.getCategories f)))
     :contributors   (map ->clj (seq (.getContributors f)))
     :entries        (map ->clj (seq (.getEntries f)))
     :entry-links    (map ->clj (seq (.getLinks f)))
     :image          (when-let [i (.getImage f)]
                       (->clj i))
     :author         (.getAuthor f)
     :copyright      (.getCopyright f)
     :description    (.getDescription f)
     :encoding       (.getEncoding f)
     :feed-type      (.getFeedType f)
     :language       (.getLanguage f)
     :link           (.getLink f)
     :published-date (.getPublishedDate f)
     :title          (.getTitle f)
     :uri            (.getUri f)
     :icon           (when-let [i (.getIcon f)]
                       (->clj i))
     :docs           (.getDocs f)
     :generator      (.getGenerator f)
     :editor         (.getManagingEditor f)
     :webmaster      (.getWebMaster f)}))


;; http://www.jdom.org/docs/apidocs/org/jdom2/Element.html
(extend-type Element
  ToClojure
  (->clj [e]
    {:tag (-> e .getQualifiedName keyword)

     :attrs
     (not-empty
      (into {}
            (map (juxt :name :value)
                 (map ->clj (.getAttributes e)))))

     :content (or (not-empty (map ->clj (.getChildren e)))
                  (not-empty (.getText e)))}))


;; http://www.jdom.org/docs/apidocs/org/jdom2/Attribute.html
(extend-type Attribute
  ToClojure
  (->clj [a]
    {:name (-> a .getQualifiedName keyword)
     :value (.getValue a)}))
