(ns project.email
  (:refer-clojure :exclude [send])
  (:require [project.template :as tpl]
            [project.env :refer [env]]
            [project.error :as e]

            [postal.core :refer [send-message]]))

(defn send-unsafe
  [to subject template & [context]]
  (let [html (tpl/render template context)
        smtp {:host (:smtp-host env)
              :port (:smtp-port env)
              :user (:smtp-user env)
              :pass (:smtp-pass env)
              :tls  (:smtp-tls  env)
              :ssl  (:smtp-ssl  env)}
        body [{:type "text/html" :content html}]
        mail {:from (:email-from env)
              :to to
              :subject subject
              :body body}]
    (send-message smtp mail)))

(defn send
  [to subject template & args]
  (try
    (let [resp (apply send-unsafe to subject template args)
          {:keys [code error message]} resp]

      (when-not (= code 0)
        (e/error! "SMTP error, code: %s, error: %s, message: %s"
                  code error message))

      resp)

    (catch Throwable e
      (e/error! "Email error, to: %s, subject: %s, template: %s, error: %s"
                to subject template (e/exc-msg e)))))
