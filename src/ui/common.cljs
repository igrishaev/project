(ns ui.common
  (:require [ui.url :as url]
            [ui.util :refer [clear-str]]))

(def js-stub "javascript:;")

(defn get-fav-url
  [feed]
  (let [{:keys [url_favicon url_source]} feed]
    (or url_favicon
        (url/get-fav-url url_source))))

(defn get-feed-title
  [feed]
  (or (some-> feed :sub :title clear-str)
      (some-> feed :title clear-str)
      (some-> feed :subtitle clear-str)
      (-> feed :url_source url/get-short-url)))

(defn get-feed-image
  [feed]
  (or (:url_image feed)
      (get-fav-url feed)))
