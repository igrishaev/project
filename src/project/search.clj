(ns project.search
  (:require [project.models :as models]
            [project.db :as db]
            [project.sync :as sync]

            [clojure.set :as set]
            [clojure.string :as str]
            [clj-http.client :as client])

  (:import org.jsoup.Jsoup
           java.io.InputStream
           java.net.URL))

(defn ->url
  [term]
  (try
    (URL. term)
    term
    (catch Throwable e)))


(defn ->base-url
  [url]
  (let [url (URL. url)
        host (.getHost url)
        prot (.getProtocol url)]
    (format "%s://%s" prot host)))


(defn domain->url
  [domain]
  (format "%s://%s" "http" domain))


(defn fetch-url
  [url]
  (let [opt {:as :stream
             :throw-exceptions true}]
    (client/get url opt)))


(defn is-html?
  [resp]
  (when-let [content-type (-> resp :headers :content-type)]
    (-> content-type
        str/lower-case
        (str/includes? "text/html"))))


(def feed-queries
  ["link[rel='alternate'][type='application/rss+xml']"
   "link[rel='alternate'][type='application/atom+xml']"
   "link[rel='alternate'][type='application/json']"])


(defn find-links
  ;; todo: cache for url
  [^InputStream stream enc url]
  (let [base-url (->base-url url)
        doc (Jsoup/parse stream enc base-url)
        select (fn [query] (.select doc query))
        elements (mapcat select feed-queries)
        links (for [el elements]
                (.absUrl el "href"))]
    (prn links) ;; todo logging
    (when-not (empty? links)
      (set links))))


(defn is-domain?
  [^String term]
  (re-matches #"[a-zA-Z0-9-\.]+" term))


(defn get-encoding
  [resp]
  (when-let [content-type (-> resp :headers :content-type)]
    (last
     (re-find
      #"charset\s*=\s*(.+)"
      (-> content-type
          str/lower-case
          str/trim)))))


(defn process-html
  [resp url]

  (let [{stream :body} resp
        encoding (get-encoding resp)
        urls (find-links
              stream encoding url)]

    (db/with-tx
      (when-not (empty? urls)
        (let [feeds (db/get-feeds-by-urls {:urls urls})
              urls-old (map :url_source feeds)
              urls-new (set/difference
                        (set urls)
                        (set urls-old))]
          (when-not (empty? urls-new)
            (doseq [url urls-new]
              (sync/sync-feed-url url))))))))


(defn process-feed
  [resp url]
  ;; todo read feed
  (sync/sync-feed-url url))


(def http-opt
  {:as :stream
   :throw-exceptions true})

(defn fix-headers
  [resp]
  (update resp :headers
          (fn [headers]
            (into {} (for [[h v] headers]
                       [(-> h str/lower-case keyword)
                        v])))))

(defn process-url
  [url]
  (let [resp (client/get url http-opt)
        resp (fix-headers resp)]

    (if (is-html? resp)
      (process-html resp url)

      (process-feed resp url))))


(defn search-sync
  [term]

  (if-let [url (->url term)]

    (when-not (models/get-feed-by-url url)
      (process-url url))

    (if (is-domain? term)
      (recur (domain->url term)))))


(defn search-results
  [term])

(defn search
  [term]

  (search-sync term)
  (search-results term))
