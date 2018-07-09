(ns project.auth
  (:require [project.env :refer [env]]
            [project.models :as models]
            [project.spec :as spec]

            [oauth.google :as google]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]

            ;; [qrfd.env :refer [env]]
            ;; [qrfd.models :as models]
            ;; [qrfd.crypt :as crypt]
            ;; [qrfd.time :as time]
            ;; [qrfd.spec :as spec]
            ;; [clojure.tools.logging :as log]
            ;; [ring.util.response :refer [redirect]]
            ))

;;
;; Common
;;

(def auth-redirect-path "/")

(defn request->user
  [request]
  (when-let [user-id (some-> request :session :user-id)]
    (models/get-user-by-id user-id)))

(defn wrap-user
  [handler]
  (fn [request]
    (let [user (request->user request)]
      (handler (assoc request :user user)))))

(defn logout
  [{:keys [session]}]
  (-> auth-redirect-path
      redirect
      (assoc :session (dissoc session :user-id))))

(defn auth-resp-ok
  [user session]
  (-> auth-redirect-path
      redirect
      (assoc :session (assoc session :user-id (:id user)))))

(defn auth-resp-err
  [message & args]
  (let [message (apply format message args)]
    (log/infof "Auth failed: %s" message)
    (-> auth-redirect-path
        redirect
        (assoc :flash message))))

;;
;; Google
;;

(defn google-init
  [request]
  (let [client-id (:auth-google-client-id env)
        back-url (:auth-google-back-url env)
        url (google/oauth-authorization-url client-id back-url)]
    (redirect url)))

(defn google-back
  [request]
  (let [{:keys [params session]} request
        {:keys [code]} params

        client-id (:auth-google-client-id env)
        client-secret (:auth-google-client-secret env)
        back-url (:auth-google-back-url env)

        oauth-token (google/oauth-access-token client-id client-secret code back-url)
        {:keys [access-token]} oauth-token

        client (google/oauth-client access-token)
        info (google/user-info client)

        user (models/upsert-google-user (merge info oauth-token))]
    (auth-resp-ok user session)))

;;
;; Email
;;

(defn email-init
  [request]
)

(defn email-back
  [request]
  (let [{:keys [params session]} request
        spec :qrfd.spec/auth-email-back] ;; todo

    (if-let [params (spec/conform :qrfd.spec/auth-email-back params)]
      (if (crypt/verify-signed-map params)
        (let [{:keys [email expires]} params]
          (if (> expires (time/epoch))
            (let [user (models/upsert-email-user {:email email})]
              (auth-resp-ok user session))
            (auth-resp-err "The URL has expired, params: %s" params)))
        (auth-resp-err "The URL signature doesn't match, params: %s" params))
      (auth-resp-err "Wrong URL parameters, params: %s" params))))
