(ns project.fetch
  ;; todo delete
  (:require [clj-http.client :as client]))

(def url-fetcher
  "http://127.0.0.1:5000")


(defn fetch
  [feed-url]
  (let [params {:as :json
                :query-params {:url feed-url}}
        result (client/get url-fetcher params)]
    (:body result)))
