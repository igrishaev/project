(ns project.search
  (:require [project.models :as models]
            [project.db :as db]
            [project.sync :as sync]
            [project.error :as e]

            [clojure.set :as set]
            [clojure.string :as str]

            [clojure.tools.logging :as log]
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
        prot (.getProtocol url)
        path (.getPath url)
        path (if (= path "/") path "")]
    (format "%s://%s%s" prot host path)))


(defn domain->url
  [domain]
  (format "%s://%s" "http" domain))


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


(defn html-find-links
  ;; todo: cache for url
  [^InputStream stream enc url]
  (let [base-url (->base-url url)
        doc (Jsoup/parse stream enc base-url)
        select (fn [query] (.select doc query))
        elements (mapcat select feed-queries)
        links (for [el elements]
                (.absUrl el "href"))]

    (when-not (empty? links)
      (set links))))


(defn is-domain?
  [^String term]
  (re-matches #"[a-zA-Z0-9-\.]+\.[a-zA-Z0-9]{2,}/?" term))


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
        urls (html-find-links
              stream encoding url)]

    (db/with-tx ;; todo without tx
      (when-not (empty? urls)
        (let [feeds (db/get-feeds-by-urls {:urls urls})
              urls-old (map :url_source feeds)
              urls-new (set/difference
                        (set urls)
                        (set urls-old))]
          (when-not (empty? urls-new)
            (doseq [url urls-new]
              (sync/sync-feed-by-url url))))

        (let [feeds (db/get-feeds-by-urls {:urls urls})]
          (mapv :id feeds))))))


(defn process-feed
  [resp url]
  ;; todo read feed from stream!
  (sync/sync-feed-by-url url)
  (let [feed (models/get-feed-by-url url)]
    [(:id feed)]))


(defn fix-headers
  [resp]
  (update resp :headers
          (fn [headers]
            (into {} (for [[h v] headers]
                       [(-> h str/lower-case keyword)
                        v])))))

(def http-opt
  {:as :stream
   :throw-exceptions true})


(defn fetch-url
  [url]
  (try
    (let [resp (client/get url http-opt)]
      (fix-headers resp))
    (catch Throwable e
      (let [data (ex-data e)
            {:keys [type status]} data]
        (log/errorf
         "HTTP error: %s, %s, %s, %s"
         url
         (e/exc-message e)
         status
         type)))))


(defn process-url
  [url]
  (if-let [resp (fetch-url url)]

    (if (is-html? resp)

      (let [result (process-html resp url)]
        (if (empty? result)

          (let [base-url (->base-url url)]
            (when (not= url base-url)
              (recur base-url)))

          result))

      (process-feed resp url))

    (let [base-url (->base-url url)]
      (when (not= url base-url)
        (recur base-url)))))


(defn search-sync
  [term]

  {:pre [(string? term)]
   :post [(or (nil? %) (vector? %))]}

  (if-let [url (->url term)]

    (if-let [feed (models/get-feed-by-url url)]
      [(:id feed)]
      (process-url url))

    (if (is-domain? term)
      (search-sync (domain->url term)))))


(defn sql-term
  [term]
  (let [sql (str/replace term #"\\|%|_" "")]
    (str "%" sql "%")))

(defn search-results
  [term feed-ids]
  (db/search-feeds-by-term
   {:feed_ids feed-ids
    :term (sql-term term)
    :limit 10}))

(defn search
  [term]
  (let [feed-ids (search-sync term)]
    (search-results term feed-ids)))
