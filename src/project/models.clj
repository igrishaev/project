(ns project.models
  (:require [project.fetch :as fetch]
            #_
            [project.db :as db])
  )

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

(defn feed->model
  [url feed]
  {:url_source url
   :url_host nil
   :url_favicon nil
   :url_image (some-> feed :feed :image :href)

   :language (some-> feed :feed :language)

   :title (some-> feed :feed :title)
   :subtitle (some-> feed :feed :subtitle)

   :link (some-> feed :feed :link)

   :upd_period (some-> feed :sy_updateperiod)
   :upd_freq (some-> feed :sy_updatefrequency)

   :date_updated_at (some-> feed :feed :updated_parsed)

   :http_status (some-> feed :status)
   :http_etag (some-> feed :headers :etag)
   :http_modified (some-> feed :headers :last-modified)

   })


(defn entry->model
  [feed_id entry]

  (let [{:keys [id
                link
                links
                author
                title
                summary
                published_parsed
                updated_parsed]} entry

        enc (map-find {:rel "enclosure"} links)
        {enc_url :href enc_mime :type} enc

        ]

    {:feed_id feed_id
     :guid id
     :link link
     :author author
     :title title
     :summary summary

     :enclosure_url enc_url
     :enclosure_mime enc_mime

     :date_published_at published_parsed
     :date_updated_at updated_parsed

     }))


(defn sync-feed
  [feed-url]
  (let [feed (fetch/fetch feed-url)

        ]
    #_
    (db/with-tx

      )
    )



  )
