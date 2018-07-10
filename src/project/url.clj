(ns project.url
  (:require [project.env :refer [env]]

            [ring.util.codec :refer [form-encode]])
  (:import java.net.URL))

(defn encode-params [params]
  (form-encode params))

(defn get-url [& [path params]]
  (cond-> (:server-base-url env)

    path
    (str path)

    params
    (str "?" (encode-params params))))

(defn get-host
  [url]
  (-> url URL. .getHost))

(defn get-fav-url
  [url]
  (format
   "https://www.google.com/s2/favicons?domain=%s&alt=feed"
   (get-host url)))
