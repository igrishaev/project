(ns project.fetcher
  (:use [project.proto :only [IFetcher]]
        )
  (:require [project.http :as http]
            [project.xml :as xml]
            [project.proto :as p]
            [project.rss :only [->RSSFeed]]

            ))

(defrecord Fetcher [url]

  IFetcher

  (fetch-feed [this]
    (let [response (http/get url)
          {:keys [status headers body]} response
          etag (get headers "Etag")
          ct (get headers "Content-Type")]
      (assoc this
             :http-body body
             :http-status status
             :http-etag etag
             :http-ct ct)))

  (guess-feed-type
    [{ct :http-ct :as this}]
    (let [feed-type
          (case ct
            "application/rss+xml; charset=utf-8"
            :feed/xml)]
      (assoc this :feed-type feed-type)))

  (parse-payload
    [{body :http-body feed-type :feed-type :as this}]
    (let [data
          (case feed-type
            :feed/xml
            (xml/parse body))]
      (assoc this :feed-data data)))

  (parse-feed
    [{data :feed-data feed-type :feed-type :as this}]
    (case
      "application/rss+xml; charset=utf-8"
      42
      ;; (-> RSSFeed. data)
      )

    )


  )
