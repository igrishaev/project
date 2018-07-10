(ns ui.url
  (:require [ui.util :as util :refer [format]]

            [cemerick.url]
            [clojure.string :as str]))



#_
(defn repr-long-url
  [url & [max-len]]
  (let [struct (cemerick.url/url url)
        struct-clean (assoc struct
                            :username nil
                            :password nil
                            :query nil
                            :anchor nil)
        result (str struct-clean)
        result (-> result (s/split #"://") second)]
    result))

#_
(defn repr-short-url
  [hash]
  (format "%s/%s" domain hash))

;; (defn format-short-url
;;   [hash]
;;   (format "%s://%s/%s" schema domain hash))

(def fav-tpl
  "https://www.google.com/s2/favicons?domain=%s&alt=feed")

(defn get-fav-url [url]
  (let [{:keys [host]} (cemerick.url/url url)]
    (format fav-tpl host)))

(defn get-short-url [url]
  (let [{:keys [host path]} (cemerick.url/url url)]
    (str host path)))

;; (defn qr-url [hash]
;;   (format "/%s/qr" hash))

;; (defn qr-url-full [hash]
;;   (format "%s://%s/%s/qr" schema domain hash))
