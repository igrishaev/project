(ns ui.routes
  (:require [re-frame.core :as rf]
            [secretary.core :as secretary
             :refer-macros [defroute]]))

;;
;; Routes
;;

(def routes-pages
  [["/"                :index]
   ["/feeds/:feed-id"  :feed]
   ["/profile"         :profile]
   ["*"                :index]])

;;
;; Inits
;;

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

(defn dispatch []
  (secretary/dispatch! (get-hash)))

(defn init-history
  []
  (secretary/set-config! :prefix "#")
  (set! (.-onhashchange js/window) dispatch))

(defn init
  []
  (init-routes)
  (init-history)
  (dispatch))
