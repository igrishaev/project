(ns project.api
  (:require [project.spec :as spec]
            [project.error :as e]
            [project.handlers :as h]
            [project.resp :as r]

            [clojure.tools.logging :as log]))

(declare actions)

(defn kw->var
  [kw]
  (-> kw str (subs 1) symbol resolve))

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
                (r/ok data))

              (r/err-spec spec params)))

          (r/err-action action)))

      (r/err-spec spec params))))

(defn handler
  [request]
  (try
    (handler-unsafe request)
    (catch Throwable e
      (log/error e)  ;; todo log better
      (r/err-server e))))

(def actions
  {:lookup {:handler :project.handlers/preview
            :spec :project.spec/api.preview}

   :demo {:handler :project.handlers/demo
          :spec :project.spec/api.demo}})
