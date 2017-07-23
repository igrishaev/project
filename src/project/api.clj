(ns project.api
  (:require [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]
            [clojure.spec.alpha :as s]
            [ring.util.response :refer [response]]))

(defn action-dispatcher
  [{action :action}]
  (cond
    (keyword? action) action
    (string? action) (-> action keyword)))

(defmulti api action-dispatcher)

(defmethod api :test
  [params]
  {:test true})

(defmethod api :default
  [params]
  params)

(defn ^:private api-handler*
  [request]

  (let [params (:body request)
        data (api params)]

    (response data)))

(def api-handler
  (-> api-handler*
      (wrap-json-body {:keywords? true})
      wrap-json-response))
