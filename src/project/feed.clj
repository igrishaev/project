(ns project.feed
  (:require [project.time :as t]
            [project.fetch :as fetch]
            [project.util :as u]

   [clojure.string :as str]))


(defn map-pred
  [params]
  (fn [other]
    (= params
       (select-keys other (keys params)))))


(defn map-find
  [params items]
  (-> (map-pred params)
      (filter items)
      first))


(defn clean-val
  [val]
  (when val
    (not-empty (str/trim val))))


(defn get-guid
  [entry]
  (let [{:keys [id
                link
                wp_uuid
                title
                summary]} entry]
    (or (clean-val id)
        (clean-val link)
        (clean-val wp_uuid)
        (clean-val title)
        (clean-val summary)
        (u/uuid))))


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
