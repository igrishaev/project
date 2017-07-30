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
