(ns project.db
  (:require [clojure.java.io :as io]
            [project.conf :refer [conf]]
            [datomic.api :as d]
            [project.uri]
            [mount.core :as mount]))

(def db-uri "datomic:mem://foobar-3") ;; todo

(mount/defstate ^:dynamic *DB*
  :start (do
           (d/create-database db-uri)
           (d/connect db-uri))
  :stop (d/release *DB*))

(defn start []
  (mount/start #'*DB*))

(defn stop []
  (mount/stop #'*DB*))

(defn query [q & args]
  (apply d/q q (d/db *DB*) args))

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
