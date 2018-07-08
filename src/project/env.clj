(ns project.env
  (:require [project.error :as e]
            [project.spec :as spec]

            [clojure.tools.logging :as log]))

(defonce env nil)

(defn load-env [& [filepath]]
  (alter-var-root
   #'env
   (constantly
    (-> filepath (or "config.edn") slurp read-string))))


(defn validate-env []
  (let [spec :project.spec/env]
    (when-not (spec/valid? spec env)
      (let [out (spec/explain-str spec env)
            msg (format "Config error, spec: %s, data: %s"
                        spec out)]
        (log/error msg)
        (e/error! msg)))))

;;
;; Init part
;;

(defn init
  []
  (validate-env))

;; Main

(load-env)
