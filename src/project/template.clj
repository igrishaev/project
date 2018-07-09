(ns project.template
  (:require [project.env :refer [env]]

            [selmer.parser :as selmer :refer [add-tag!]]
            [markdown.core :as md]
            [clojure.java.io :as io]))

(selmer/set-resource-path! (io/resource "templates"))

(defonce ^:dynamic *context* nil)

;; (defmacro with-context
;;   [data & body]
;;   `(binding [*context* ~data]
;;      ~@body))

;; (defn wrap-context
;;   [handler]
;;   (fn [request]
;;     (with-context {:request request}
;;       (handler request))))

(def md-params
  [:heading-anchors true
   :reference-links? true
   :footnotes? true])

(defn tag-markdown
  [args context-map content]
  (let [body (get-in content [:markdown :content])]
    (apply md/md-to-html-string body md-params)))

(defn render
  ([path]
   (render path nil))
  ([path params]
   (let [ctx {:env env}]
     (selmer/render-file path (merge params ctx *context*)))))

(add-tag! :markdown tag-markdown :endmarkdown)

(defn init-cache []
  (if (:tpl-cache-on env)
    (selmer.parser/cache-on!)
    (selmer.parser/cache-off!)))

(defn init []
  (init-cache))
