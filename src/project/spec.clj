(ns project.spec
  (:require [project.uri :refer [is-uri?]]
            [clojure.spec.alpha :as s]))

;; (defn is-uri?
;;   [val]
;;   (try
;;     (let [url (java.net.URI. val)]
;;       ;; has at least scheme and host specified
;;       (and (-> url .getScheme not-empty)
;;            (-> url .getHost not-empty)))
;;     (catch Exception e)))

;; user input

(defn spec-error [spec data]
  (s/explain-data spec data))

(s/def ::feed_url string?)

;; db.feed

(s/def :feed/url-source is-uri?)
(s/def :feed/url-site is-uri?)
(s/def :feed/url-icon is-uri?)
(s/def :feed/date-last-sync inst?)
(s/def :feed/date-next-sync inst?)
(s/def :feed/tags (s/coll-of string?))

;; api

;; (s/def ::action
;;   (s/or :string string?
;;         :keyword keyword?))

(s/def ::action string?)

(s/def ::base-api.in
  (s/keys :req-un [::action]))

(s/def :preview-feed/in
  (s/keys :req-un [::feed_url]))

(s/def :preview-feed/out
  (s/keys :req [:feed/url-source
                :feed/url-site
                :feed/url-icon
                :feed/date-last-sync
                :feed/date-next-sync
                :feed/tags]))
