(ns project.db
  (:require [clojure.java.io :as io]
            [project.conf :refer [conf]]
            [datomic.api :as d]
            [project.uri :as uri]
            [mount.core :as mount]))

(def db-uri "datomic:dev://localhost:4334/hello") ;; todo

(mount/defstate ^:dynamic *DB*
  :start (do
           (d/create-database db-uri)
           (d/connect db-uri))
  :stop (d/release *DB*))

(defn start []
  (mount/start #'*DB*))

(defn stop []
  (mount/stop #'*DB*))

(defn q [query & args]
  (apply d/q query (d/db *DB*) args))

(defn transact
  [data]
  @(d/transact *DB* data))

(defn read-edn
  [filename]
  (-> filename
      io/resource
      slurp
      read-string))

(defn transact-edn
  [filename]
  (transact (read-edn filename)))

(defn prepare []
  (doseq [file ["scheme/01-initial.edn"
                "scheme/fixtures.edn"]]
    (transact-edn file)))

(defn get-feed-by-url [feed_url]
  (q '[:find (pull ?feed [* {:message/_feed [*]}]) .
       :in $ ?url
       :where [?feed :feed/url-source ?url]]
     (uri/read-uri feed_url)))
