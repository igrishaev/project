(ns project.http
  (:require [clj-http.client :as client]
            #_
            [project.conf :refer [conf]]
            [clj-http.conn-mgr
             :refer [make-reusable-conn-manager
                     shutdown-manager]]

            #_
            [mount.core :as mount]

            )
  (:refer-clojure :exclude [get]))

(declare CM)

(defn- on-start []
  (let [params {}

        #_{:timeout (:http-timeout conf)
           :threads (:http-threads conf)}]

    (make-reusable-conn-manager params)))

(defn- on-stop []
  (shutdown-manager CM))

;; todo refactor everything

(defonce CM nil)

#_
(mount/defstate CM
  :start (on-start)
  :stop (on-stop))

#_
(defn start! []
  (mount/start #'CM))

#_
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
       #_
       (assoc-in [:headers "User-Agent"]
                 (:http-user-agent conf)))))

(def get (partial request :get))

(def head (partial request :head))
