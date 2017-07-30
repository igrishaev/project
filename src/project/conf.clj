(ns project.conf
  (:require [cprop.source :refer [from-resource
                                  from-file
                                  from-env
                                  from-system-props]]
            [mount.core :as mount])
  (:import java.util.MissingResourceException))

(defn- on-start []
  (merge (try
           (from-resource)
           (catch MissingResourceException e))
         (try
           (from-file)
           (catch MissingResourceException e))
         (from-env)
         (from-system-props)))

(mount/defstate conf
  :start (on-start)
  :stop nil)

(defn start! []
  (mount/start #'conf))

(defn stop! []
  (mount/stop #'conf))
