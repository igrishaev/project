(ns project.http
  (:require [clj-http.client :as client])
  (:refer-clojure :exclude [get]))

(defn request [method url & [params]]
  (client/request
   (assoc params
          :method method
          :throw-exceptions true
          :url url)))

(def get (partial request :get))

(def head (partial request :head))
