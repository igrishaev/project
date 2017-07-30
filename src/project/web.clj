(ns project.web
  (:require [project.conf :refer [conf]]
            [project.api :refer [api-handler]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [mount.core :as mount]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes app-routes
  (GET "/" [] "<h1>index</h1>")
  (POST "/api" request (api-handler request))
  (route/not-found "<h1>Page not found</h1>"))

(defn- get-jetty-params []
  {:port (:jetty-port conf)
   :join? false})

(declare APP)

(defn- on-start []
  (let [params (get-jetty-params)
        port (:port params)]
    (log/infof "Starting web server on port %s..." port)
    (let [server (run-jetty app-routes params)]
      (log/info "Server started.")
      server)))

(defn- on-stop []
  (log/info "Stopping web server...")
  (.stop APP)
  (log/info "Server stopped.")
  nil)

(mount/defstate APP
  :start (on-start)
  :stop (on-stop))

(defn start! []
  (mount/start #'APP))

(defn stop! []
  (mount/stop #'APP))
