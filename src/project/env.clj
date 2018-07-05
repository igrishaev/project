(ns project.env
  #_
  (:require [qrfd.error :refer [error!]]
            [clojure.tools.logging :as log]
            [clojure.spec.alpha :as s]
            [qrfd.spec :as spec]))

(defonce env nil)

(defn load-env [& [filepath]]
  (alter-var-root
   #'env
   (constantly
    (-> filepath (or "config.edn") slurp read-string))))

#_
(defn validate-env []
  (let [spec :qrfd.spec/env]
    (when-not (s/valid? spec env)
      (let [out (s/explain-str spec env)
            msg (format "Config error, spec: %s, data: %s" spec out)]
        (log/error msg)
        (error! msg)))))

;;
;; Init part
;;

(load-env)

#_
(defn init
  []
  (validate-env))
