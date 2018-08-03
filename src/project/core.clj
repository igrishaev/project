(ns project.core
  (:require [project.env :as env]
            [project.db :as db]
            [project.template :as tpl]
            [project.queue :as queue]
            [project.server :as server])
  (:gen-class))


(defn start []
  (env/init)
  (db/init)
  (tpl/init)
  (queue/init)
  (server/init))


(defn stop []
  (server/stop)
  (queue/stop))


(defn -main
  [& args]
  (start))
