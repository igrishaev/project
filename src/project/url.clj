(ns project.url
  (:require [project.env :refer [env]]

            [ring.util.codec :refer [form-encode]]))

(defn encode-params [params]
  (form-encode params))

(defn get-url [& [path params]]
  (cond-> (:server-base-url env)

    path
    (str path)

    params
    (str "?" (encode-params params))))
