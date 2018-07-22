(ns project.auth
  (:require [project.env :refer [env]]
            [project.models :as models]
            [project.spec :as spec]
            [project.error :as e]
            [project.crypt :as crypt]
            [project.email :as email]
            [project.time :as time]
            [project.resp :as r]
            [project.url :as url]

            [oauth.google :as google]
            [clojure.tools.logging :as log]
            [ring.util.response :refer [redirect]]))


;;
;; Common
;;

;; TODO change this path

(def auth-redirect-path "/dev")


(defn request->user
  [request]
  (when-let [user-id (some-> request :session :user-id)]
    (prn "user found" user-id) ;; TODO remove
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
  (let [{user-id :id} user
        session (assoc session :user-id user-id)]

    (-> (redirect auth-redirect-path)
        (assoc :session session))))


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

        {:keys [auth-google-client-id
                auth-google-client-secret
                auth-google-back-url]} env

        oauth-token (google/oauth-access-token
                     auth-google-client-id
                     auth-google-client-secret
                     auth-google-back-url)

        {:keys [access-token]} oauth-token

        client (google/oauth-client access-token)
        info (google/user-info client)

        user (models/upsert-google-user oauth-token info)]

    (auth-resp-ok user session)))


;;
;; Email
;;


(def email-sent-ok
  "Message has been sent. Check you mailbox and follow the secret link.")


(def email-sent-err
  "We could not send a message to this address. Please try later.")


(defn email-init
  [request]
  (let [{:keys [params]} request
        {:keys [email-login-expire]} env
        spec :project.spec.email/auth-init
        params* (spec/conform spec params)]

    (if params*
      (let [{:keys [email]} params*
            expires (+ (time/epoch) email-login-expire)
            to-sign {:email email :expires expires}
            data (crypt/sign-map to-sign)
            subject "Project | Login"
            template "email/login.html"
            url (url/get-url "/auth/email/back" data)
            context {:url url}]

        (try
          (email/send email subject template context)
          (r/ok-message email-sent-ok)

          (catch Throwable e
            (let [err-msg (e/exc-msg e)]
              (log/errorf "Email error: %s, addr: %s" err-msg email)
              (r/err 500 email-sent-err err-msg)))))

      (r/err-spec spec params))))


(defn email-back
  [request]
  (let [{:keys [params session]} request
        spec :project.spec.email/auth-back]

    (if-let [params* (spec/conform spec params)]

      (if (crypt/verify-signed-map params*)
        (let [{:keys [email expires]} params*]
          (if (< (time/epoch) expires)
            (let [user (models/upsert-email-user {:email email})]
              (auth-resp-ok user session))

            ;; TODO refactor those responses; single function

            (auth-resp-err "The login URL has expired.")))
        (auth-resp-err "The login URL is malformed."))
      (auth-resp-err "The login URL is malformed."))))
