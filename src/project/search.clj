(ns project.search
  (:require [project.models :as models]

            [clj-http.client :as client])

  (:import org.jsoup.Jsoup
           java.io.InputStream
           java.net.URL))

(defn as-url
  [term]
  (try
    (URL. term)
    (catch Throwable e)))


(defn base-url
  [^URL url]
  (let [host (.getHost url)
        prot (.getProtocol url)]
    (URL.
     (format "%s://%s" prot host))))


(defn domain->url
  [domain]
  (format "%s://%s" "http" domain))


(defn fetch-url
  [url]
  (let [opt {:as :stream
             :throw-exceptions true}]
    (client/get (str url) opt)))


(defn is-html?
  [headers]
  (when-let [content-type (get headers "content-type")]
    (-> content-type .toLowerCase (.contains "text/html"))))


(defn find-links
  [^InputStream stream enc base-url]
  (let [doc (Jsoup/parse stream enc base-url)
        queries ["link[rel='alternate'][type='application/rss+xml']"
                 "link[rel='alternate'][type='application/atom+xml']"
                 "link[rel='alternate'][type='application/json']"]
        select (fn [query] (.select doc query))
        elements (mapcat select queries)
        links (for [el elements]
                (.absUrl el "href"))]
    (when-not (empty? links)
      (set links))))


(defn is-domain?
  [^String term]
  (re-matches #"[a-zA-Z0-9-\.]+" term))

(defn is-feed?
  [resp]
  )


(defn process-html
  [resp]
  (let [{stream :stream} resp
        url "sssss"
        encoding "dddd"]
    (find-links stream encoding url)))


(defn process-feed
  [resp]
  )


(def http-opt
  {:as :stream
   :throw-exceptions true})

(defn process-url
  [url]
  (let [resp (client/get url http-opt)]

    (if (is-feed? resp)
      (process-feed resp)

      (if (is-html? resp)
        (process-html resp)))))


(defn search-sync
  [term]

  (if-let [url (as-url term)]

    (when-not (models/get-feed-by-url url)
      (process-url url))

    (if (is-domain? term)
      (search (domain->url term)))))


(defn search-results
  [term])

(defn search
  [term]

  (search-sync term)
  (search-results term))
