(ns project.sync
  (:require [project.fetch :as fetch]
            [project.db :as db]
            [project.time :refer [parse-iso-now]]
            [medley.core :refer [distinct-by]])
  (:import java.net.URL))

(defn get-host
  [url]
  (-> url URL. .getHost))

(defn get-google-favicon
  [url]
  (format "https://www.google.com/s2/favicons?domain=%s&alt=feed"
          (get-host url)))

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
    (not-empty (clojure.string/trim val))))


(defn get-entry-id
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
        (clean-val summary))))


(defn data->feed
  [url data]
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

        {image_href :href} image

        ]

    {:url_source url
     :url_host (get-host url)
     :url_favicon (get-google-favicon url)
     :url_image image_href

     :language language

     :title title
     :subtitle subtitle

     :link link

     :upd_period sy_updateperiod
     :upd_freq sy_updatefrequency

     :date_updated_at (parse-iso-now updated_parsed)

     :http_status status
     :http_etag etag
     :http_modified last-modified

     :parse_ok (= bozo 0)
     :parse_err bozo_exception

     }))

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
        {enc_url :href enc_mime :type} enc

        ]

    {:guid (get-entry-id entry)
     :link link
     :author author
     :title title
     :summary summary

     :enclosure_url enc_url
     :enclosure_mime enc_mime

     :date_published_at (parse-iso-now published_parsed)
     :date_updated_at (parse-iso-now updated_parsed)

     }))

(defn sync-feed
  [feed-url]
  (let [data (fetch/fetch feed-url)]

    (let [feed (data->feed feed-url data)

          entries (map data->entry (:entries data))
          entries (distinct-by :guid entries)]

      (db/with-tx

        (let [[{feed-id :id}] (db/upsert-feed feed)]

          (when-not (empty? entries)
            (db/upsert-entry
             (for [e entries]
               (assoc e :feed_id feed-id)))))))))

(defn sync-user
  [user_id]
  (let [query
        "
insert into messages (sub_id, entry_id)

select s.id, e.id
from subs s
join feeds f on f.id = s.feed_id
join entries e on e.feed_id = f.id
left join messages m on
  m.entry_id = e.id and m.sub_id = s.id and not m.deleted
where
s.user_id = 1
and m.id is null
and not s.deleted
and not f.deleted
and not e.deleted
"]
    (db/execute! [query user_id])))


(defn batch-import
  []
  (with-open [rdr (clojure.java.io/reader "rss.txt")]
    (doseq [line (line-seq rdr)]
      (println line)
      (sync-feed line))))
