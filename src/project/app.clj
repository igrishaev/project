(ns project.app
  (:require [project.api :as api]
            [project.env :refer [env]]
            [project.auth :as auth]

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

            ))

(defroutes app-naked

  (POST "/api" request (api/handler request))

  (context
   "/auth" []

   (GET "/logout" request (auth/logout request))

   (context
    "/google" []
    (GET "/init" request (auth/google-init request))
    (GET "/back" request (auth/google-back request)))

   (context
    "/email" []
    (GET "/init" request (auth/email-init request))
    (GET "/back" request (auth/email-back request))))

  )

(def opt-cookie
  {:secure    (:cookie-secure    env)
   :http-only (:cookie-http-only env)
   :max-age   (:cookie-max-age   env)})

(def opt-session
  {:store (cookie-store {:key (:cookie-session-key env)})
   :cookie-attrs opt-cookie
   :cookie-name (:cookie-session-name env)})

(def app
  (-> app-naked

      ;; tpl/wrap-context
      auth/wrap-user

      (wrap-session opt-session)

      wrap-keyword-params
      wrap-params
      wrap-json-params
      wrap-json-response

      ;;views/wrap-exception

      ;; (wrap-resource "public")
      ;; (wrap-webjars "/webjars")

      ))
