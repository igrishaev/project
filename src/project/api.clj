(ns project.api
  (:require [project.db :as db]
            [project.spec :as spec :refer [spec-error]]
            [project.feed :as feed]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [response]]
            project.json))

(defn preview-feed
  [{:keys [feed_url]}]
  (if-let [feed (db/get-feed-by-url feed_url)]
    feed
    (let [data (feed/fetch-feed feed_url)]
      (feed/save-feed feed_url data)
      (db/get-feed-by-url feed_url))))

(def actions
  {"preview-feed" preview-feed})

(defn raise [& args]
  (throw (Exception. (clojure.string/join \space args))))

(defn call-action
  [params]
  (let [spec :project.spec/base-api.in
        error (spec-error spec params)]
    (when error
      (raise "wrong input" error)))
  (let [action (:action params)
        func (get actions action)
        schema-in (keyword action "in")
        schema-out (keyword action "out")]
    (when-not func
      (raise "wrong action" action))
    (when-let [error (spec-error schema-in params)]
      (raise "wrong input" error))
    (let [result (func params)]
      (when-let [error (spec-error schema-out result)]
        (raise "wrong output" error))
      result)))

(defn ^:private api-handler*
  [{:keys [body]}]
  (try
    (response (call-action body))
    (catch Exception e
      {:status 500
       :body (.getMessage e)})))

(def api-handler
  (-> api-handler*
      (wrap-json-body {:keywords? true})
      wrap-json-response))
