(ns project.web
  (:require [project.conf :refer [conf]]
            [project.api :refer [api-handler]]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [mount.core :as mount]
            [ring.adapter.jetty :refer [run-jetty]]))

(defroutes app-routes
  (GET "/" [] "<h1>index</h1>")
  (POST "/api" request (api-handler request))
  (route/not-found "<h1>Page not found</h1>"))

(defn- get-jetty-params []
  {:port (:jetty-port conf)
   :join? false})

(mount/defstate ^:dynamic *app*
  :start (run-jetty app-routes (get-jetty-params))
  :stop (.stop *app*))

(defn start! []
  (mount/start #'*app*))

(defn stop! []
  (mount/stop #'*app*))
