(ns project.views
  (:require [project.template :as tpl]

            [ring.util.response :refer [response]]))

(defn view-index
  [request]
  (response (tpl/render "index.html")))

(defn view-dev
  [request]
  (response (tpl/render "dev/index.html")))
