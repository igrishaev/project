(ns project.app
  (:require [project.api :as api]

            [compojure.core :refer [context defroutes GET POST]]

            ;; [ring.middleware.webjars :refer [wrap-webjars]]

            [compojure.route :as route]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.session.cookie :refer [cookie-store]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.json :refer
             [wrap-json-response wrap-json-params]]

            ;; [qrfd.env :refer [env]]
            ;; [qrfd.views :as views]
            ;; [qrfd.hooks :as hooks]
            ;; [qrfd.auth :as auth]
            ;; [qrfd.template :as tpl]
            ;; [qrfd.api :as api]
            ;; qrfd.time
            ))

(defroutes app-naked

  (POST "/api" request (api/handler request))

  )

(def app
  (-> app-naked

      ;; tpl/wrap-context
      ;; auth/wrap-user
      ;; views/wrap-session-id

      ;; (wrap-session opt-session)

      wrap-keyword-params
      wrap-params
      wrap-json-params
      wrap-json-response

      ;;views/wrap-exception

      ;; (wrap-resource "public")

      ;; (wrap-webjars "/webjars")

      ))
