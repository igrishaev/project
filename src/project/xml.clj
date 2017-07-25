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

(def find-updated (partial find-node :updated))

(def find-published (partial find-node :published))

(def find-description (partial find-node :description))

(def find-image (partial find-node :image))

(def find-icon (partial find-node :icon))

(def find-logo (partial find-node :logo))

(def find-url (partial find-node :url))

(def find-summary (partial find-node :summary))

(def find-subtitle (partial find-node :subtitle))

(def find-content (partial find-node :content))

(def find-link (partial find-node :link))

(def find-categories (partial find-nodes :category))

(def find-items (partial find-nodes :item))

(def find-entries (partial find-nodes :entry))

(def find-author (partial find-node :author))

(def find-contributor (partial find-node :contributor))

(def find-name (partial find-node :name))

(def find-dc-creator (partial find-node :dc:creator))

(def find-guid (partial find-node :guid))

(def find-id (partial find-node :id))

(def find-enclosures (partial find-nodes :enclosure))

(defn first-content [node]
  (-> node :content first))
