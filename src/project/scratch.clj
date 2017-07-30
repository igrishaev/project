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


;; favicon discovery


#_(defn parse-uri [link]
  (safe
   (let [uri (URI. link)
         scheme (.getScheme uri)
         host (.getHost uri)
         path (.getPath uri)]
     {:scheme scheme
      :host host
      :path path})))

#_(defn get-favicon-url
  [{:keys [scheme path]}]

  )

#_(defn discover-favicon [parsed]
  (when-let [url (get-favicon-url parsed)]
    (when (url-exists? url)
      url)))

#_(defn discover [link]


  (when-let [parsed (parse-uri link)]

    (or (discover-favicon parsed)
        (discover-manifest parsed))


    (if-let [favicon-url (get-favicon-url parsed)]
      (when (url-exists? favicon-url)
        favicon-url)
)))


#_(defn is-full-url [url]
  (when-let [{:keys [scheme host] :as parsed}
             (parse-uri url)]
    (and
     (-> scheme not-empty)
     (-> host not-empty)
     true)))


#_(defn get-url [path link]

  (let [{:keys [scheme host] :as parsed}
        (parse-uri link)]
    (when (validate-full-url parsed)
      (str (URI. scheme host path "")))))

#_(def get-favicon-url (partial get-url "/favicon.ico"))

#_(def get-manifest-url (partial get-url "/manifest.json"))

#_(defn get-url-from-manifest [url]
  (let [data (-> url
                 (http/get {:as :json})
                 :body)]
    (-> data
        :icons
        last
        :src)))


#_(defn make-absolute-path [path]
  (let [slash \/]
    (if (-> path first (= slash))
      path
      (str slash path))))

#_(defn get-url-from-manifest [link]
  (when-let [url (get-manifest-url link)]
    (when-let [path (get-url-from-manifest url)]
      (if (is-full-url path)
        path
        (let [abs-path (make-absolute-path path)]
          (get-url abs-path link))))))
