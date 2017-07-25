(ns project.atom
  "Atom parser."
  (:require [project.xml :as xml])
  (:use [project.proto :only [Feed Entity Media Tag]]))

(deftype AtomTag [node]

  Tag

  (get-tag-name [this]
    (or
     (-> node :attrs :label)
     (-> node :attrs :term))))

(deftype AtomItem [node]

  Entity

  (get-entity-title [this]
    (-> node
        xml/find-title
        xml/first-content))

  (get-entity-link [this] ;; todo
    #_(-> node
        xml/find-link
        xml/first-content)
    nil)

  (get-entity-description [this] ;; todo
    (-> node
        xml/find-content
        (or (-> node xml/find-summary))
        xml/first-content))

  (get-entity-guid [this]
    (-> node
        xml/find-id
        xml/first-content))

  (get-entity-author [this]
    (-> node
        xml/find-author
        (or (-> node xml/find-contributor))
        xml/find-name
        xml/first-content))

  (get-entity-pub-date [this]
    (-> node
        xml/find-updated
        (or (-> node xml/find-published))
        xml/first-content))

  (get-entity-tags [this]
    (->> node
         xml/find-categories
         (mapv ->AtomTag)))

  (get-entity-media [this] ;; todo
    #_(->> node
         xml/find-entries
         (mapv ->RSSMedia))
    []))


(deftype AtomFeed [node]

  Feed

  (get-feed-lang [this]
    (-> node
        :attrs
        :xml:lang))

  (get-feed-title [this]
    (-> node
        xml/find-title
        xml/first-content))

  (get-feed-pub-date [this]
    (-> node
        xml/find-updated
        xml/first-content))

  (get-feed-icon [this]
    (-> node
        xml/find-icon
        xml/first-content))

  (get-feed-image [this]
    (-> node
        xml/find-logo
        xml/first-content))

  (get-feed-description [this]
    (-> node
        xml/find-subtitle
        xml/first-content))

  (get-feed-tags [this]
    (->> node
         xml/find-categories
         (mapv ->AtomTag)))

  (get-feed-entities [this]
    (->> node
         xml/find-entries
         (mapv ->AtomItem))))
