(ns project.server
  (:require [project.web :refer [app-routes]]
            [clojure.test :refer :all]
            [mount.core :as mount]
            [compojure.route :as route]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.resource :refer [wrap-resource]]))

(def dummy (route/not-found ""))

(def feed-routes
  (-> dummy
      (wrap-resource "public")
      wrap-not-modified
      wrap-content-type))

(def jetty-params
  {:port 4000
   :join? false})

(mount/defstate ^:dynamic *feed-server*
  :start (run-jetty feed-routes jetty-params)
  :stop (.stop *feed-server*))

(defn start! []
  (mount/start #'*feed-server*))

(defn stop! []
  (mount/stop #'*feed-server*))
