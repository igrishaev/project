(ns project.rss
  "RSS parser."
  (:require [project.xml :as xml]))

(defn parse-2-0-enclosure [node]
  (:attrs node))

(defn parse-2-0-category [node]
  (-> node
      xml/first-content))

(defn parse-2-0-item [node]
  (let [item-nodes (:content node)

        title (-> item-nodes
                  xml/find-title
                  xml/first-content)

        description (-> item-nodes
                        xml/find-description
                        xml/first-content)

        author (-> item-nodes
                   xml/find-author
                   xml/first-content)


        link (-> item-nodes
                 xml/find-link
                 xml/first-content)

        language (-> item-nodes
                     xml/find-language
                     xml/first-content)

        guid (-> item-nodes
                 xml/find-guid
                 xml/first-content)

        category-nodes (-> item-nodes
                           xml/find-categories)

        enclosure-nodes (-> item-nodes
                            xml/find-enclosures)

        pub-date (-> item-nodes
                    xml/find-pub-date
                    xml/first-content)


        ]
    {:title title
     :description description
     :author author
     :link link
     :language language
     :guid guid
     :pubDate pub-date
     :enclosures (map parse-2-0-enclosure enclosure-nodes)
     :categories (map parse-2-0-category category-nodes)}))

(defn parse-2-0 [node]
  (let [channel-nodes (-> node
                          :content
                          xml/find-channel
                          :content)

        title (-> channel-nodes
                  xml/find-title
                  xml/first-content)

        description (-> channel-nodes
                        xml/find-description
                        xml/first-content)

        link (-> channel-nodes
                 xml/find-link
                 xml/first-content)

        image-nodes (-> channel-nodes
                        xml/find-image
                        :content)

        image-url (-> image-nodes
                      xml/find-url
                      xml/first-content)

        image-title (-> image-nodes
                        xml/find-title
                        xml/first-content)

        image-link (-> image-nodes
                       xml/find-link
                       xml/first-content)

        language (-> channel-nodes
                     xml/find-language
                     xml/first-content)

        category-nodes (-> channel-nodes
                           xml/find-categories)

        item-nodes (-> channel-nodes
                       xml/find-items)

        pub-date (-> item-nodes
                     xml/find-pub-date
                     xml/first-content)


        last-build-date (-> item-nodes
                            xml/find-last-build-date
                            xml/first-content)

        ttl (-> item-nodes
                xml/find-ttl
                xml/first-content)]

    {:title title
     :description description
     :link link
     :ttl ttl
     :pubDate pub-date
     :lastBuildDate last-build-date
     :image {:url image-url
             :title image-title
             :link image-link}
     :language language
     :categories (map parse-2-0-category category-nodes)
     :items (map parse-2-0-item item-nodes)}))

(defn parse [node]
  (let [version (-> node :attrs :version)]
    (cond
      (= version "2.0")
      (parse-2-0 node)

      :else
      (-> "wrong RSS version: %s"
          (format version)
          Exception.
          throw))))
