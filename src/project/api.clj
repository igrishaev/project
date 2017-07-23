(ns project.api
  (:require [project.db :as db]
            [project.feed :as feed]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]
            [clojure.spec.alpha :as s]
            [ring.util.response :refer [response]]
            project.json))

;; todo

(defn preview-feed
  [{:keys [feed_url]}]
  (if-let [feed (db/get-feed-by-url feed_url)]
    feed
    (let [data (feed/fetch-feed feed_url)]
      (feed/save-feed feed_url data)
      (db/get-feed-by-url feed_url))))

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

  (response (preview-feed (:body request)))

  ;; (let [params (:body request)
  ;;       data (api params)]

  ;;   (response data))
  )

(def api-handler
  (-> api-handler*
      (wrap-json-body {:keywords? true})
      wrap-json-response))
