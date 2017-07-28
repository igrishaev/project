(ns project.rss
  "RSS parser."
  (:require [project.xml :as xml])
  (:use [project.proto :only [Feed Entity Media Tag]]))

(deftype RSSTag [node]

  Tag

  (get-tag-name [this]
    (-> node xml/first-content)))

(deftype RSSMedia [node]

  Media

  (get-media-url [this]
    (-> node
        :attrs
        :url))

  (get-media-type [this]
    (-> node
        :attrs
        :type))

  (get-media-size [this]
    (-> node
        :attrs
        :length)))

(deftype RSSItem [node]

  Entity

  (get-entity-title [this]
    (-> node
        xml/find-title
        xml/first-content))

  (get-entity-link [this]
    (-> node
        xml/find-link
        xml/first-content))

  (get-entity-description [this]
    (-> node
        xml/find-description
        xml/first-content))

  (get-entity-guid [this]
    (-> node
        xml/find-guid
        xml/first-content))

  (get-entity-author [this]
    (-> node
        xml/find-author
        (or (-> node xml/find-dc-creator))
        xml/first-content))

  (get-entity-pub-date [this]
    (-> node
        xml/find-pub-date
        xml/first-content))

  (get-entity-tags [this]
    (->> node
         xml/find-categories
         (mapv ->RSSTag)))

  (get-entity-media [this]
    (->> node
         xml/find-enclosures
         (mapv ->RSSMedia))))

(deftype RSSFeed [node]

  Feed

  (get-feed-lang [this]
    (-> node
        xml/find-channel
        xml/find-language
        xml/first-content))

  (get-feed-title [this]
    (-> node
        xml/find-channel
        xml/find-title
        xml/first-content))

  (get-feed-link [this]
    (-> node
        xml/find-channel
        xml/find-link
        xml/first-content))

  (get-feed-pub-date [this]
    (-> node
        xml/find-channel
        xml/find-pub-date
        xml/first-content))

  (get-feed-icon [this]
    )

  (get-feed-image [this]
    (-> node
        xml/find-channel
        xml/find-image
        xml/find-url
        xml/first-content))

  (get-feed-description [this]
    (-> node
        xml/find-channel
        xml/find-description
        xml/first-content))

  (get-feed-tags [this]
    (->> node
         xml/find-channel
         xml/find-categories
         (mapv ->RSSTag)))

  (get-feed-entities [this]
    (->> node
         xml/find-channel
         xml/find-items
         (mapv ->RSSItem))))
