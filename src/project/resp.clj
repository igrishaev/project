(ns project.resp
  (:require [project.spec :as spec]
            [project.error :as e]))

(defn resp
  ([data]
   (resp 200 data))

  ([status data]
   {:status status
    :body data}))

(defn ok
  [data]
  (resp data))

(defn err
  [status message & [data]]
  (resp status {:error true
                :error-message message
                :error-data data}))

;;
;; Shortcuts
;;

(defn ok-message
  [message & args]
  (resp {:message (apply format message args )}))

(defn err-spec
  [spec data]
  (let [debug (spec/explain-str spec data)]
    (err 400
         "Wrong input parameters"
         debug)))

(defn err-anon
  []
  (err 403
       "You should be logged in to access this API."))

(defn err-feed-deleted
  []
  (err 404
       "This feed has been deleted and cannot be queried."))

(defn err-server
  [e]
  (err 500
       "Internal server error"
       (e/exc-msg e)))

(defn err-action
  [action]
  (err 404
       (format "Action '%s' was not found" action)))
