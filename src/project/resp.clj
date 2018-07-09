(ns project.resp
  (:require [project.spec :as spec]
            [project.error :as e]))

(defn ok [data]
  data)

(defn err
  [code text & [data]]
  {:error-code code
   :error-text text
   :error-data data})

(defn guess-code
  [data]
  (if (and (map? data))
    (case (:error-code data)
      nil 200
      (:wrong-params :input-params :bad-request) 400
      :not-found 404
      500)
    200))

(defn data->resp
  [data]
  {:status (guess-code data)
   :body data})

(defn err-spec
  [spec data]
  (data->resp
   (let [out (spec/explain-str spec data)]
     (err :wrong-params
          "Input data is incorrect"
          out))))

(defn err-server
  [e]
  (data->resp
   (err :server-error
        "Internal server error"
        (e/exc-msg e))))

(defn err-action
  [action]
  (data->resp
   (err :not-found
        (format "Action '%s' was not found" action))))
