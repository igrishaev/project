(ns project.feed
  (:require [project.time :as t]
            [project.fetch :as fetch]
            [project.util :as u]

            [clojure.string :as str]

            [medley.core :refer [distinct-by]]))


(defn clean-str
  [val]
  (when val
    (not-empty (str/trim val))))


(defn data->entry
  [entry]

  (let [{:keys [link
                href
                links
                author
                title
                summary
                published_parsed
                updated_parsed]} entry

        enc (map-find {:rel "enclosure"} links)
        {enc_url :href enc_mime :type} enc]

    {:guid (get-guid entry)
     :link link
     :author author
     :title title
     :summary summary

     :enclosure_url enc_url
     :enclosure_mime enc_mime

     :date_published_at (t/parse-iso-now published_parsed)
     :date_updated_at (t/parse-iso-now updated_parsed)}))


(defn data->feed
  [data]
  (let [{:keys [feed
                entries

                bozo
                bozo_exception

                status
                headers

                sy_updateperiod
                sy_updatefrequency

                ]} data

        {:keys [etag last-modified]} headers

        {:keys [language
                title
                subtitle
                link
                updated_parsed
                image]} feed

        {image_href :href} image]

    {:url_image image_href
     :language language

     :title title
     :subtitle subtitle

     :link link

     :upd_period sy_updateperiod
     :upd_freq sy_updatefrequency

     :date_updated_at (t/parse-iso-now updated_parsed)

     :last_entry_count (count entries)

     :http_status status
     :http_etag etag
     :http_modified last-modified

     :parse_ok (= bozo 0)
     :parse_err bozo_exception}))


(defn fetch-feed
  [feed]

  (let [{feed-url :url_source feed-id :id} feed
        data (fetch/fetch feed-url)
        {:keys [entries]} data]

    ;; TODO cleanup nils

    (let [feed (data->feed data)
          entries (map data->entry entries)
          entries (distinct-by :guid entries)]

      {:feed feed
       :entries entries})))

;;
;; Rome
;;


(defn rome->feed
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

     :title title
     :subtitle description

     :link link

     ;; TODO deal with HTTP status and headers

     ;; :upd_period sy_updateperiod
     ;; :upd_freq sy_updatefrequency

     :date_updated_at published-date

     :last_entry_count (count entries)

     ;; :http_status status
     ;; :http_etag etag
     ;; :http_modified last-modified

     ;; :parse_ok (= bozo 0)
     ;; :parse_err bozo_exception

     }))


(defn rome->entry
  [entry]

  (let [{:keys [
                link
                uri
                description
                author
                title

                ;; TODO save all enclosures
                enclosures

                ;; TODO save categories
                categories

                updated-date
                published-date

                ]} entry

        summary (:value description)

        enclosure (first enclosures)]

    {:guid (or (clean-str uri)
               (clean-str link)
               (clean-str title)
               (clean-str summary)
               (u/uuid))

     ;; TODO think more on :guid

     :link link
     :author (clean-str author)
     :title title
     :summary summary

     :enclosure_url (:url enclosure)
     :enclosure_mime (:type enclosure)

     :date_published_at published-date
     :date_updated_at updated-date

     }))
