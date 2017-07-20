(ns project.db
  (:require [environ.core :refer [env]]
            [clojure.java.jdbc :as jdbc]
            [conman.core :as conman]
            [mount.core :as mount]))

(defn ^:private get-pool-spec []
  {:jdbc-url (env :database-url)})

(mount/defstate ^:private ^:dynamic *db*
  :start (do
           (conman/bind-connection *db* "queries.sql")
           (conman/connect! (get-pool-spec)))
  :stop (conman/disconnect! *db*))

(defn start! []
  (mount/start #'*db*))

(defn stop! []
  (mount/stop #'*db*))

(defn query [& args]
  (apply jdbc/query *db* args))

(defn execute! [& args]
  (apply jdbc/execute! *db* args))

(defn insert! [& args]
  (apply jdbc/insert! *db* args))

(defn insert-multi! [& args]
  (apply jdbc/insert-multi! *db* args))

(defn update! [& args]
  (apply jdbc/update! *db* args))

(defn delete! [& args]
  (apply jdbc/delete! *db* args))

(defmacro with-trx [& body]
  `(conman/with-transaction [*db*]
     ~@body))

(defmacro with-trx-test [& body]
  `(conman/with-transaction [*db*]
     (jdbc/db-set-rollback-only! *db*)
     ~@body))
