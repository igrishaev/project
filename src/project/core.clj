(ns project.core
  (:require [project.env :as env]
            [project.db :as db]
            [project.server :as server])
  (:gen-class))

(defn init []
  (env/init)
  (db/init)
  (server/init))

(defn -main
  [& args]
  (init))
