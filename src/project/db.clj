(ns project.db
  (:require [project.conf :refer [conf]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [conman.core :as conman]
            [mount.core :as mount]
            [honeysql.core :as sql]
            [honeysql.format :refer [format-clause]]
            clj-time.jdbc))

(defn- get-pool-spec []
  {:jdbc-url (conf :database-url)})

(declare DB)

(defn- on-start []
  (log/info "Starting database...")
  (let [db (conman/connect! (get-pool-spec))]
    (log/info "Started.")
    db))

(defn- on-stop []
  (log/info "Stopping database...")
  (conman/disconnect! DB)
  (log/info "Database stopped.")
  nil)

(mount/defstate DB
  :start (on-start)
  :stop (on-stop))

(conman/bind-connection DB "queries.sql")

(defn start! []
  (mount/start #'DB))

(defn stop! []
  (mount/stop #'DB))

(defn query [& args]
  (apply jdbc/query DB args))

(defn execute! [& args]
  (apply jdbc/execute! DB args))

(defn insert! [& args]
  (apply jdbc/insert! DB args))

(defn insert-multi! [& args]
  (apply jdbc/insert-multi! DB args))

(defn update! [& args]
  (apply jdbc/update! DB args))

(defn delete! [& args]
  (apply jdbc/delete! DB args))

(defmacro with-trx [& body]
  `(conman/with-transaction [DB]
     ~@body))

(defmacro with-trx-test [& body]
  `(conman/with-transaction [DB]
     (jdbc/db-set-rollback-only! DB)
     ~@body))

;;
;; Upsert
;;

(def into-map (partial into {}))

(defn get-fields
  [values]
  (cond
    (map? values) (keys values)
    (vector? values) (keys (first values))
    :else (throw (Exception. "todo"))))

(defn values-excluded [fields]
  (into-map
   (for [field fields]
     [field (sql/raw (format "EXCLUDED.%s" (name field)))])))

(defn upsert!
  [constraint table values]
  (let [map-insert {:insert-into table :values values}
        [query1 & params] (sql/format map-insert)
        fields (get-fields values)
        map-update {:set (values-excluded fields)}
        [query2] (sql/format map-update)
        q (format "%s ON CONFLICT ON constraint %s DO UPDATE %s"
                  query1 constraint query2)
        query (concat [q] params)]
    (execute! query)))

(def upsert-feed (partial upsert! "feeds_url_source_unique"))

(def upsert-entry (partial upsert! "entries_feed_guid_unique"))
