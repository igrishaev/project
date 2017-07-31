#_(defn parse-xml [payload]
  (let [node (xml/parse payload)
        tag (:tag node)]
    (cond
      (= tag :rss)
      (rss/parse node)

      (and (= tag :feed)
           (= (-> node :attrs :xmlns)
              "http://www.w3.org/2005/Atom"))
      (atom/parse node)

      :else
      (-> "wrong XML tag: %s"
          (format tag)
          Exception.
          throw))))

#_(case get-content-type

    :application/rss+xml
    :feed/rss

    :application/atom+xml
    :feed/atom

    :text/xml
    (let [ns-atom "http://www.w3.org/2005/Atom"
          node (xml/parse payload)]
      (cond
        (-> node :tag (= :rss))
        (RSSFeed. node)

        (and (-> node :tag (= :rss))
             (-> node :attrs :xmlns (= ns-atom)))
        (AtomFeed. node)

        :else
        (raise "todo")))

    (raise "todo"))

#_(defprotocol IFetcher
  (fetch-response [this])
  (guess-feed-type [this])
  (parse-payload [this])
  (parse-feed [this]))


#_(defrecord MFetcher [url]

  IFetcher

  (fetch-response
    [this]
    (let [response (http/get url)]
      (assoc this
             :http-status 200
             :http-payload "123123"
             :http-headers "dsdfsdf"
             :http-etag "asdsd"
             :http-expires "asdsd"
             :http-ct :text/xml)))

  (guess-feed-type
    [{ct :http-ct :as this}]
    (let [feed-type
          (case ct
            :application/rss+xml
            :feed/rss

            :application/atom+xml
            :feed/atom

            :application/json
            :feed/json

            (:text/xml :application/xml)
            (guess-xml-type this))]
      (assoc this :feed-type feed-type)))

  (guess-xml-type
    [{payload :http-payload :as this}]

    )

  (parse-payload
    [{:keys [feed-type payload] :as this}]
    (let [parsed
          (case feed-type
            (:feed/rss :feed/atom)
            (xml/parse payload)

            :feed/json
            (json/parse payload))]
      (assoc this :parsed parsed)))


)
