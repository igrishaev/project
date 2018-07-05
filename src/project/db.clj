(ns project.db
  (:require [project.conf :refer [conf]]
            [clojure.java.jdbc :as jdbc]
            [clojure.tools.logging :as log]
            [conman.core :as conman]
            [mount.core :as mount]
            [honeysql.core :as sql]
            [honeysql.format :refer [format-clause]]
            clj-time.jdbc))

(defn map-entry [k v]
  (clojure.lang.MapEntry/create k v))

(defmethod format-clause :on-conflict
  [[_ {:keys [constraint
              do-update]}] bar]
  (format
   "ON CONFLICT %s DO %s"
   (format-clause (map-entry :constraint constraint) bar)
   (format-clause (map-entry :do-update do-update) bar)))

(defmethod format-clause :constraint
  [[_ foo] _]
  (str foo))

(defn get-fields
  [values]
  (cond
    (map? values) (keys values)
    (vector? values) (keys (first values))))

(defmethod format-clause :do-update
  [[_ foo] bar]
  (format-clause (map-entry :set (for [field (-> bar :values get-fields)]
                                   [field
                                    (sql/raw (format "EXCLUDED.%s" (name field)))]))
                 bar)

  )

#_
(defmethod format-clause :insert-into [[_ table] _]
  (if (and (sequential? table) (sequential? (first table)))
    (str "INSERT INTO "
         (to-sql (ffirst table))
         " (" (comma-join (map to-sql (second (first table)))) ") "
         (binding [*subquery?* false]
           (to-sql (second table))))
    (str "INSERT INTO " (to-sql table))))


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
