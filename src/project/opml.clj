(ns project.opml
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]

            [medley.core :refer [distinct-by]]))


(defn read-zip
  [src]
  (let [zipper (-> src xml/parse zip/xml-zip)]
    zipper))


(defn zip-seq
  [loc]
  (->> loc
       (iterate zip/next)
       (take-while (complement zip/end?))))


(defn get-tag
  [loc]
  (let [node (-> loc
                 zip/up
                 zip/node)
        {:keys [attrs]} node]
    (or (:title attrs)
        (:text attrs))))


(defn get-feed
  [loc]
  (let [node (zip/node loc)
        {:keys [attrs]} node]
    (when (:xmlUrl attrs)
      {:url (:xmlUrl attrs)
       :link (:htmlUrl attrs)
       :title (or (:title attrs)
                  (:text attrs))})))


(defn __get-feeds
  [zip-seq]
  (reduce
   (fn [result loc]
     (let [node (zip/node loc)]
       (if-let [feed (get-feed loc)]
         (let [tag (get-tag loc)
               feed (assoc feed :tag tag)]
           (conj result feed))
         result)))
   []
   zip-seq))


(defn get-feeds
  [zip-seq]
  (not-empty
   (distinct-by :url (__get-feeds zip-seq))))


(defn read-feeds
  [src]
  (-> src read-zip zip-seq get-feeds))


(defn string->stream
  ([s] (string->stream s "UTF-8"))
  ([s encoding]
   (-> s
       (.getBytes encoding)
       (java.io.ByteArrayInputStream.))))


(defn read-feeds-from-string
  [src]
  (-> src string->stream read-feeds))


(defn read-feeds-from-file
  [src]
  (-> src (java.io.File.) read-feeds))
