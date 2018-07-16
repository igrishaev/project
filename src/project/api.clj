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
        spec :project.spec/api.base]

    (if user

      (if-let [params* (spec/conform spec params)]

        (let [{:keys [action]} params*
              rule (get actions action)]

          (if rule
            (let [{:keys [handler spec]} rule
                  handler (kw->var handler)
                  params* (spec/conform spec params)]

              (prn params*)

              (if params*
                (handler params* user)

                (r/err-spec spec params)))

            (r/err-action action)))

        (r/err-spec spec params))

      (r/err-anon))))

(defn handler
  [request]
  (try
    (handler-unsafe request)
    (catch Throwable e
      (log/error e)  ;; todo log better
      (r/err-server e))))

(def actions

  {:search-feeds {:handler :project.handlers/search-feeds
                  :spec :project.spec/api.search-feeds}

   :update-subscription {:handler :project.handlers/update-subscription
                         :spec :project.spec/api.update-subscription}

   :subscribe {:handler :project.handlers/subscribe
               :spec :project.spec/api.subscribe}

   :unsubscribe {:handler :project.handlers/unsubscribe
                 :spec :project.spec/api.unsubscribe}

   :subscriptions {:handler :project.handlers/subscriptions
                   :spec :project.spec/api.subscriptions}

   :messages {:handler :project.handlers/messages
              :spec :project.spec/api.messages}

   :mark-read {:handler :project.handlers/mark-read
               :spec :project.spec/api.mark-read}

   :user-info {:handler :project.handlers/user-info
               :spec :project.spec/api.user-info}})
