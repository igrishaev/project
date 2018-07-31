(ns project.http
  (:require [clj-http.client :as client]
            [clj-http.conn-mgr
             :refer [make-reusable-conn-manager
                     shutdown-manager]])
  (:refer-clojure :exclude [get]))


(def user-agent
  "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.3396.99 Safari/537.36")


(def conn-manager
  (make-reusable-conn-manager
   {:timeout 5
    :threads 4
    :insecure? true}))


(def opt-default
  {:throw-exceptions true
   :connection-manager conn-manager
   :headers {"User-Agent" user-agent}})


(defn deep-merge
  [& vals]
  (letfn [(map-or-nil? [x]
            (or (map? x) (nil? x)))]
    (if (every? map-or-nil? vals)
      (apply merge-with deep-merge vals)
      (if (every? sequential? vals)
        (apply concat vals)
        (last vals)))))


(defn request [method url & [opt]]
  (client/request
   (deep-merge opt opt-default
               {:method method :url url})))


(def get (partial request :get))

(def post (partial request :post))
