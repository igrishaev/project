(ns ui.routes
  (:require [re-frame.core :as rf]
            [secretary.core :as secretary
             :refer-macros [defroute]]))

;;
;; Routes
;;

(def routes-pages
  [["/"                :index]
   ["/auth"            :auth]
   ["/feeds/:feed-id"  :feed]
   ["/profile"         :profile]
   ["*"                :index]])


(defn init-routes
  []
  (doseq [[path page] routes-pages]
    (secretary/add-route!
     path
     (fn [params]
       (rf/dispatch [:ui.events/page page params])))))


(defn get-hash []
  (-> js/window .-location .-hash))


(defn set-hash [hash]
  (set! (-> js/window .-location .-hash) hash))


(defn goto
  [path & _]
  (set-hash path))


(defn dispatch []
  (secretary/dispatch! (get-hash)))


(defn init-history
  []
  (secretary/set-config! :prefix "#")
  (set! (.-onhashchange js/window) dispatch))

;;
;; Init
;;

(defn init
  []
  (init-routes)
  (init-history)
  (dispatch))
