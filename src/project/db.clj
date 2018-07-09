(ns project.db
  (:require [project.env :refer [env]]

            [clojure.java.jdbc :as jdbc]
            [clj-time.jdbc] ;; extends JDBC protocols
            [honeysql.core :as sql]
            [honeysql.format :as f :refer [format-clause]]
            [cheshire.core :as json] ;; todo to json ns
            [clojure.tools.logging :as log]
            [migratus.core :as migratus])

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
;; Extend HoneySql
;;

(defmethod format-clause :returning
  [[_ fields] _]
  (str
   "RETURNING "
   (f/comma-join (map f/to-sql fields))))

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

(defmethod pgobj->clj "json"
  [pgobj]
  (-> pgobj .getValue (json/parse-string true)))

(defn clj->pgobject
  [data]
  (doto (PGobject.)
    (.setType "jsonb")
    (.setValue (json/generate-string data))))

(extend-protocol jdbc/IResultSetReadColumn

  PGobject
  (result-set-read-column [pgobj metadata index]
    (pgobj->clj pgobj)))

(extend-protocol jdbc/ISQLValue

  clojure.lang.Keyword
  (sql-value [val]
    (-> val str (subs 1)))

  java.net.URL
  (sql-value [val]
    (str val))

  clojure.lang.IPersistentMap
  (sql-value [val]
    (clj->pgobject val)))

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

(defn values-excluded [fields]
  (into-map
   (for [field fields]
     [field (sql/raw (clojure.core/format
                      "EXCLUDED.%s" (name field)))])))

(defn get-fields
  [values]
  (-> values first keys set (disj :id)))

(defn to-vect
  [values]
  (if (map? values)
    [values]
    values))

(defn upsert!
  [table constraint values]
  (let [values (to-vect values)
        map-insert {:insert-into table :values values}
        [query1 & params] (sql/format map-insert)
        fields (get-fields values)
        vals-exc (values-excluded fields)
        vals-exc (assoc vals-exc :updated_at :%now)
        map-update {:set vals-exc}
        [query2] (sql/format map-update)
        q (clojure.core/format
           "%s ON CONFLICT %s DO UPDATE %s RETURNING id"
           query1 constraint query2)
        q-vector (concat [q] params)]
    (query q-vector)))

(def upsert-user
  (partial upsert! :users "(email)"))

(def upsert-feed
  (partial upsert! :feeds "(url_source)"))

(def upsert-entry
  (partial upsert! :entries "(feed_id, guid)"))

(def upsert-subs
  (partial upsert! :subs "(feed_id, user_id)"))

(def upsert-message
  (partial upsert! :messages "(sub_id, entry_id)"))

;;
;; Migrations
;;

(def ^:private
  mg-cfg {:store :database
          :migration-dir "migrations"
          :db *db*})

(defn migrate []
  (log/info "Running migrations...")
  (migratus/migrate mg-cfg)
  (log/info "Migrations done."))

(defn rollback
  []
  (migratus/rollback mg-cfg))

;;
;; Init part
;;

(defn init []
  (migrate))
