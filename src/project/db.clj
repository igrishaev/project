(ns project.db
  (:require [clojure.java.jdbc :as jdbc]
            [clj-time.jdbc] ;; extends JDBC protocols
            [honeysql.core :as sql]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [migratus.core :as migratus]
            [project.env :refer [env]])
  (:refer-clojure :exclude [format])
  (:import org.postgresql.util.PGobject))

(def ^:dynamic
  *db*
  {:dbtype   "postgresql"
   :host     (:db-host env)
   :port     (:db-port env)
   :dbname   (:db-database env)
   :user     (:db-user env)
   :password (:db-password env)})

;;
;; DB types
;;

(defmulti pgobj->clj
  (fn [pgobj] (.getType pgobj)))

(defmethod pgobj->clj :default
  [pgobj]
  (-> pgobj .getValue))

(defmethod pgobj->clj "jsonb"
  [pgobj]
  (-> pgobj .getValue (json/parse-string true)))

(extend-protocol jdbc/IResultSetReadColumn

  PGobject
  (result-set-read-column [pgobj _metadata _index]
    (pgobj->clj pgobj)))

(extend-protocol jdbc/ISQLValue

  clojure.lang.Keyword
  (sql-value [val]
    (-> val str (subs 1)))

  java.net.URL
  (sql-value [val]
    (str val)))

;;
;; Helpers
;;

(def format sql/format)
(def raw sql/raw)

;;
;; DB API
;; Here and below: partial doesn't work with binding.
;;

(defn query [& args]
  (apply jdbc/query *db* args))

(defn get-by-id [& args]
  (apply jdbc/get-by-id *db* args))

(defn find-by-keys [& args]
  (apply jdbc/find-by-keys *db* args))

(defn insert! [& args]
  (first (apply jdbc/insert! *db* args)))

(defn insert-multi! [& args]
  (apply jdbc/insert-multi! *db* args))

(defn update! [& args]
  (apply jdbc/update! *db* args))

(defn delete! [& args]
  (apply jdbc/delete! *db* args))

(defn execute! [& args]
  (apply jdbc/execute! *db* args))

(defmacro with-tx
  "Runs a series of queries into transaction."
  [& body]
  `(jdbc/with-db-transaction [tx# *db*]
     (binding [*db* tx#]
       ~@body)))

(defmacro with-tx-test
  "The same as `with-tx` but rolls back the transaction after all."
  [& body]
  `(with-tx
     (jdbc/db-set-rollback-only! *db*)
     ~@body))

;;
;; Upsert
;;

(def into-map (partial into {}))

(defn get-fields
  [values]
  (keys (first values)))

(defn values-excluded [fields]
  (into-map
   (for [field fields]
     [field (sql/raw (clojure.core/format
                      "EXCLUDED.%s" (name field)))])))

(defn upsert!
  [table constraint values]
  (let [map-insert {:insert-into table :values values}
        [query1 & params] (sql/format map-insert)
        fields (get-fields values)
        map-update {:set (values-excluded fields)}
        [query2] (sql/format map-update)
        q (clojure.core/format
           "%s ON CONFLICT ON constraint %s DO UPDATE %s RETURNING id"
           query1 constraint query2)
        q-vector (concat [q] params)]
    (query q-vector)))

#_
(defn upsert-feed
  [params]
  (upsert! :feeds "feeds_url_source_unique" params))

(def upsert-feed
  (partial upsert! :feeds "feeds_url_source_unique"))

(def upsert-entry
  (partial upsert! :entries "entries_feed_guid_unique"))

;;
;; Migrations
;;

(def ^:private
  mg-cfg {:store :database
          :migration-dir "migrations"
          :db *db*})

(defn- migrate []
  (log/info "Running migrations...")
  (migratus/migrate mg-cfg)
  (log/info "Migrations done."))

;;
;; Init part
;;

(defn init []
  (migrate))
