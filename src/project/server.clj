(ns project.server
  (:require [project.app :as app]
            [project.env :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]]))

(defonce server nil)

(defn alter-server [srv]
  (alter-var-root #'server (constantly srv)))

(defn start []
  (let [port (:server-port env)
        opt {:port port :join? false}
        server (run-jetty #'app/app opt)]
    (alter-server server)))

(defn stop []
  (when server
    (.stop server)
    (alter-server nil)))

(defn init []
  (start))
