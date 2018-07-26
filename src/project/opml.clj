(ns project.opml
  (:require [clojure.zip :as zip]
            [clojure.xml :as xml]))


(def _p "/Users/ivan/Downloads/feedly-54e0a565-255d-4ed8-b831-024b437488bf-2018-07-17.opml")


(defn read-zip
  [src]
  (let [zipper (-> src xml/parse zip/xml-zip)]
    zipper))


(defn zip-seq
  [loc]
  (when-not (zip/end? loc)
    (cons loc (zip-seq (zip/next loc)))))


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


(defn get-feeds
  [zip-seq]
  (reduce
   (fn [result loc]
     (let [node (zip/node loc)]
       (if-let [feed (get-feed loc)]
         (let [tag (get-tag loc)]
           (update result tag conj feed))
         result)))
   {}
   zip-seq))


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
