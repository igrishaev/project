(ns project.db
  (:require [project.env :refer [env]]

            [hugsql.core :as hug]
            [conman.core :as conman]
            [clojure.java.jdbc :as jdbc]
            [clj-time.jdbc]
            [clj-time.coerce :as c]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [migratus.core :as migratus])

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
    (clj->pgobject val))

  java.util.Date
  (sql-value [val]
    (-> val c/from-date c/to-sql-time)))

;;
;; JDBC
;; Here and below: partial doesn't work with binding.
;;

(defn query [& args]
  (apply jdbc/query *db* args))

(defn get-by-id [& args]
  (apply jdbc/get-by-id *db* args))

(defn find-by-keys [& args]
  (apply jdbc/find-by-keys *db* args))

(defn find-first [& args]
  (first (apply find-by-keys args)))

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


(defn create-migration
  [name]
  (migratus/create mg-cfg name))


(defn rollback
  []
  (migratus/rollback mg-cfg))

;;
;; Upsert
;;

(def join-comma (partial clojure.string/join ", "))

(defn join-lines
  [& lines]
  (clojure.string/join "\n" lines))

(def to-vec (partial apply vector))

(defn upsert!
  [table conflict model]
  (let [fields (keys model)
        juxter (apply juxt fields)
        values (juxter model)
        fields (map name fields)]
    (first
     (query
      (to-vec
       (join-lines
        ""
        (format "insert into %s (" (name table))
        (join-comma fields)
        ")"
        "values ("
        (join-comma (repeat (count fields) "?"))
        ")"
        (format "on conflict %s" conflict)
        "do update"
        "set"
        "updated_at = now(),"
        (join-comma
         (for [field fields :when (not= field "id")]
           (format "%s = excluded.%s" field field)))
        "returning *")
       values)))))


(defn upsert-multi!
  [table conflict models]
  (let [fields (keys (first models))
        juxter (apply juxt fields)
        values (mapcat juxter models)
        fields (map name fields)]
    (query
     (to-vec
      (join-lines
       ""
       (format "insert into %s (" (name table))
       (join-comma fields)
       ")"
       "values"
       (join-comma
        (repeat
         (count models)
         (format "(%s)"
                 (join-comma (repeat (count fields) "?")))))
       (format "on conflict %s" conflict)
       "do update"
       "set"
       "updated_at = now(),"
       (join-comma
        (for [field fields :when (not= field "id")]
          (format "%s = excluded.%s" field field)))
       "returning *")
      values))))

;;
;; Init part
;;

(defn init []
  (migrate))

;;
;; Main
;;

(conman/bind-connection *db* "sql/queries.sql")
(conman/bind-connection *db* "sql/sync.sql")

(hug/def-sqlvec-fns "sql/queries.sql")

nil
