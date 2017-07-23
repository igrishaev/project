(ns project.spec
  (:require [clojure.spec.alpha :as s]))

(defn is-uri?
  [val]
  (try
    (let [url (java.net.URI. val)]
      ;; has at least scheme and host specified
      (and (-> url .getScheme not-empty)
           (-> url .getHost not-empty)))
    (catch Exception e)))

(s/def ::url
    (s/and string? is-uri?))

(s/def ::id int?)
(s/def ::date_created_at inst?)
(s/def ::date_updated_at inst?)

(s/def ::date_last_sync inst?)
(s/def ::date_next_sync inst?)

(s/def ::title string?)
(s/def ::description string?)

(s/def ::description string?)

    ;; url_src         text not null default '' unique,
    ;; url_site        text not null default '',
    ;; url_favicon     text not null default '',
    ;; url_banner      text not null default '',
    ;; last_update_ok  boolean not null default false,
    ;; last_update_msg text not null default '',
    ;; update_count    integer not null default 0,
    ;; message_count   integer not null default 0,
    ;; active          boolean not null default true


(s/def ::preview-source-in
  (s/keys :req-un [::url]))


;; (defn ^{:tr :ui/message}
;;   foo
;;   [x]
;;   (= x 42)
;;   )
