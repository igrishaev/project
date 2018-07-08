(ns project.api
  (:require [project.spec :as spec]
            [project.error :as e]
            [project.handlers :as h]

            [clojure.tools.logging :as log]))

(defn guess-code
  [data]
  (case (:error-code data)
    nil 200
    :wrong-params 400
    :not-found 404
    500))

(defn data->resp
  [data]
  {:status (guess-code data)
   :body data})

(defn err-spec
  [spec data]
  (data->resp
   (let [out (spec/explain-str spec data)]
     (h/err :wrong-params
            "Input data is incorrect"
            out))))

(defn err-server
  [e]
  (data->resp
   (h/err :server-error
          "Internal server error"
          (e/exc-msg e))))

(defn err-action
  [action]
  (data->resp
   (h/err :not-found
          (format "No `%s` action was found" action))))

(defn kw->var
  [kw]
  (-> kw str (subs 1) symbol resolve))

(declare actions)

(defn handler-unsafe
  [request]
  (let [{:keys [params user]} request
        spec :project.spec/api.base
        params* (spec/conform spec params)]

    (if params*

      (let [{:keys [action]} params*
            rule (get actions action)]

        (if rule
          (let [{:keys [handler spec]} rule
                handler (kw->var handler)
                params* (spec/conform spec params)]

            (if params*
              (let [data (handler params* user)]
                (data->resp data))

              (err-spec spec params)))

          (err-action action)))

      (err-spec spec params))))

(defn handler
  [request]
  (try
    (handler-unsafe request)
    (catch Throwable e
      (log/error e)  ;; todo log better
      (err-server e))))

(def actions
  {:lookup {:handler :project.handlers/preview
            :spec :project.spec/api.preview}

   :demo {:handler :project.handlers/demo
          :spec :project.spec/api.demo}})
