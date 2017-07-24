(ns project.api
  (:require [project.db :as db]
            [project.spec :as spec]
            [project.feed :as feed]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-body]]
            ;; [clojure.spec.alpha :as s]
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

(defn error [& args]
  (throw (Exception. (clojure.string/join \space args))))

(defn call-action
  [params]
  (let [spec-base :project.spec/base-api.in
        [ok result] (spec/validate spec-base params)]
    (if ok
      (let [action (:action params)
            schema-in (keyword action "in")
            schema-out (keyword action "out")
            func (get actions action)
            ;; result (func params)
            ]
        (if func
          (let [])
          1
          (error "wrong action" action)

          )
        )
      (error "wrong data structure" result)))


  ;; (let [spec-in (keyword action )]

  ;;   )

  ;; (if-let [func (get actions action)
  ;;          ;; spec-in ::foo
  ;;          ]
  ;;   #_(if (s/valid? spec-in params)
  ;;     (let [result (func params)
  ;;           spec-out ::foo2]
  ;;       (if (s/valid? spec-out result)
  ;;         result
  ;;         (let [reason (s/explain-str spec-out result)]
  ;;           (error "wrong out data" reason))))
  ;;     (let [reason (s/explain-str spec-in params)]
  ;;       (error "wrong in data" reason)))
  ;;   (error "wrong action" action))
  )

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
