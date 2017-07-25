(ns project.proto)

(defprotocol Feed
  (get-feed-lang [this])
  (get-feed-title [this])
  (get-feed-pub-date [this])
  (get-feed-tags [this])
  (get-feed-icon [this])
  (get-feed-image [this])
  (get-feed-description [this])
  (get-feed-entities [this]))

(defprotocol Entity
  (get-entity-title [this])
  (get-entity-link [this])
  (get-entity-description [this])
  (get-entity-guid [this])
  (get-entity-author [this])
  (get-entity-media [this])
  (get-entity-pub-date [this])
  (get-entity-tags [this]))

(defprotocol Media
  (get-media-url [this])
  (get-media-type [this])
  (get-media-size [this]))

(defprotocol Tag
  (get-tag-name [this]))
