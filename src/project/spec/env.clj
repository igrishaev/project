(ns project.spec.env
  (:require [clojure.spec.alpha :as s])
  (:import java.net.URL))

;;
;; Env
;;

(defn url? [url]
  (when (string? url)
    (try
      (URL. url)
      (catch Throwable e))))

(defn session-key?
  [key]
  (when (string? key)
    (= (count key) 16)))

(s/def ::auth-google-back-url url?)
(s/def ::auth-google-client-id string?)
(s/def ::auth-google-client-secret string?)
(s/def ::cookie-http-only boolean?)
(s/def ::cookie-max-age int?)
(s/def ::cookie-secure boolean?)
(s/def ::cookie-session-key session-key?)
(s/def ::cookie-session-name string?)
(s/def ::crypt-key string?)
(s/def ::db-database string?)
(s/def ::db-host string?)
(s/def ::db-password string?)
(s/def ::db-port int?)
(s/def ::db-user string?)
(s/def ::email-from string?)
(s/def ::email-login-expire int?)
(s/def ::google-analytics string?)
(s/def ::google-api-key string?)
(s/def ::server-base-url url?)
(s/def ::server-port int?)
(s/def ::smtp-host string?)
(s/def ::smtp-pass string?)
(s/def ::smtp-port int?)
(s/def ::smtp-ssl boolean?)
(s/def ::smtp-tls boolean?)
(s/def ::smtp-user string?)
(s/def ::tpl-cache-on boolean?)
(s/def ::ui-entry-page-limit pos?)


(s/def ::env
  (s/keys
   :req-un
   [
    ::auth-google-back-url
    ::auth-google-client-id
    ::auth-google-client-secret
    ::cookie-http-only
    ::cookie-max-age
    ::cookie-secure
    ::cookie-session-key
    ::cookie-session-name
    ::crypt-key
    ::db-database
    ::db-host
    ::db-password
    ::db-port
    ::db-user
    ::email-from
    ::email-login-expire
    ::google-analytics
    ::google-api-key
    ::server-base-url
    ::server-port
    ::smtp-host
    ::smtp-pass
    ::smtp-port
    ::smtp-ssl
    ::smtp-tls
    ::smtp-user
    ::tpl-cache-on
    ::ui-entry-page-limit
    ]))
