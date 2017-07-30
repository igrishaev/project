(ns project.test.fixtures
  (:require [project.test.server :as server]))

(defn server-fixture [f]
  (server/start!)
  (f)
  (server/stop!))
