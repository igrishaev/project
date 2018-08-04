(ns project.nrepl
  (:require [clojure.tools.nrepl.server
             :refer (start-server stop-server)]))


(defn nrepl-handler []
  (require 'cider.nrepl)
  (ns-resolve 'cider.nrepl 'cider-nrepl-handler))


(defonce __server (atom nil))

(def set-server! (partial reset! __server))


(def port 7888)

(defn start
  []
  (when-not @__server
    (set-server!
     (start-server :port port :handler (nrepl-handler)))))


(defn stop
  []
  (when-let [server @__server]
    (stop-server server)
    (set-server! nil)))


(defn init
  []
  (start))
