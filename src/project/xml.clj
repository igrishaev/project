(ns project.xml
  "temp XML parsing utilities."
  (:require [clojure.xml :as xml]
            [clojure.java.io :as io]))

(defn parse [string]
  (let [stream (-> string .getBytes io/input-stream)]
    (xml/parse stream)))

(defn tag-selector [tag]
  (fn [node]
    (-> node :tag (= tag))))

(defn find-nodes [tag node]
  (filter (tag-selector tag) (:content node)))

(defn find-node [tag node]
  (first (find-nodes tag node)))

(def find-channel (partial find-node :channel))

(def find-title (partial find-node :title))

(def find-language (partial find-node :language))

(def find-pub-date (partial find-node :pubDate))

(def find-description (partial find-node :description))

(def find-image (partial find-node :image))

(def find-url (partial find-node :url))

(def find-link (partial find-node :link))

(def find-categories (partial find-nodes :category))

(def find-items (partial find-nodes :item))

(def find-author (partial find-node :author))

(def find-dc-creator (partial find-node :dc:creator))

(def find-guid (partial find-node :guid))

(def find-enclosures (partial find-nodes :enclosure))



(defn first-content [node]
  (-> node :content first))

;; (defn get-node [tag node]
;;   (->> node
;;        :content
;;        (filter (tag-selector tag))))

;; (def find-channel (partial find-node :channel))

;;



;; (def find-url (partial find-node :url))



;; (def find-last-build-date (partial find-node :lastBuildDate))



;;

;; (def find-ttl (partial find-node :ttl))

;;

;; (def find-items (partial find-nodes :item))



;;



;; (defn get-feed-title [nodes]
;;   (some-> nodes
;;           find-title-node
;;           first-content
;;           (or "")))

;; (defn process-rss-2 [xml]
;;   (let [channel (->> xml :content first )]
;;     (some->> channel :content (find-first-tag :pubDate)
;; )

;;     )


;;   )

;; (defn update-source [source]
;;   (let [request {:method :get :url (:url_src source)}
;;         response (http/request request)
;;         body (:body response)
;;         ctype (-> response :headers (get "Content-Type"))
;;         ]

;;     (case ctype
;;       "application/xml"
;;       (proces-xml-data body)

;;       )


;;     )

;; )

;; (defn proces-xml-data [body]
;;   (let [stream (-> body .getBytes io/input-stream)
;;         xml (xml/parse stream)
;;         ]
;;     (cond
;;       (and
;;        (-> xml :tag (= :rss))
;;        (-> xml :attrs :version (= "2.0")))
;;       (process-rss-2 xml)

;;       )
;; )
;;   )

;; (case ctype
;;       "application/xml"
;;       (proces-xml-data body)

;;       )
