(ns project.http
  (:require [clj-http.client :as client]
            [project.conf :refer [conf]]
            [clj-http.conn-mgr
             :refer [make-reusable-conn-manager
                     shutdown-manager]]
            [mount.core :as mount])
  (:refer-clojure :exclude [get]))

(declare CM)

(defn- on-start []
  (let [params {:timeout (:http-timeout conf)
                :threads (:http-threads conf)}]
    (make-reusable-conn-manager params)))

(defn- on-stop []
  (shutdown-manager CM))

(mount/defstate CM
  :start (on-start)
  :stop (on-stop))

(defn start! []
  (mount/start #'CM))

(defn stop! []
  (mount/stop #'CM))

;; todo exception

(defn request [method url & [params]]
  (client/request
   (-> params
       (assoc :method method
              :connection-manager CM
              :throw-exceptions true
              :url url)
       (assoc-in [:headers "User-Agent"]
                 (:http-user-agent conf)))))

(def get (partial request :get))

(def head (partial request :head))
