(ns project.feed
  (:require [project.time :as t]
            [project.rome :as rome]
            [project.util :as u]
            [project.sanitize :as san]

            [clojure.string :as str]
            [medley.core :refer [distinct-by]])
  (:import java.net.URL))


(defn to-abs-url
  [base-url path]
  (str (URL. (URL. base-url) path)))


(defn clean-str
  [val]
  (when val
    (not-empty (str/trim val))))


;;
;; Rome data to feed
;;

(defn data->feed
  [data]
  (let [{:keys [title
                language
                description

                ;; TODO process categories
                categories

                link

                image
                entries

                published-date

                ]} data

        {url_image :url} image

        ]

    {:url_image url_image
     :language language

     :title (san/san-none title)
     :subtitle (san/san-none description)

     :link link

     ;; TODO deal with HTTP status and headers

     ;; :upd_period sy_updateperiod
     ;; :upd_freq sy_updatefrequency

     :date_updated_at (or published-date (t/now))

     :last_entry_count (count entries)

     ;; :http_status status
     ;; :http_etag etag
     ;; :http_modified last-modified

     ;; :parse_ok (= bozo 0)
     ;; :parse_err bozo_exception

     }))


(defn data->entry
  [entry]

  (let [{:keys [
                link
                uri
                description
                author
                title

                ;; TODO save all enclosures
                enclosures
                contents

                ;; TODO save categories
                categories

                updated-date
                published-date

                ]} entry

        summary (:value description)

        enclosure (first enclosures)

        page-url (or link uri)

        {enclosure_url :url
         enclosure_mime :type} enclosure

        enclosure_url
        (when enclosure_url
          (to-abs-url page-url enclosure_url))

        content (or (some-> contents first :value)
                    summary)]

    {:guid (or (clean-str uri)
               (clean-str link)
               (clean-str title)
               (clean-str summary)
               (u/uuid))

     :link link
     :author (clean-str author)
     :title (san/san-none title)

     :summary (san/san-html content page-url)

     :enclosure_url enclosure_url
     :enclosure_mime enclosure_mime

     :date_published_at (or published-date (t/now))
     :date_updated_at (or updated-date (t/now))

     }))


;;
;; Fetch
;;


(defn fetch-feed
  [feed-url]

  (let [data (rome/parse-url feed-url)
        {:keys [entries]} data]

    ;; TODO cleanup nils

    (let [feed (data->feed data)
          entries (map data->entry entries)
          entries (distinct-by :guid entries)]

      {:feed feed
       :entries entries})))
