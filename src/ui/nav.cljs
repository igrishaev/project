(ns ui.nav
  (:require [re-frame.core :as rf]
            [secretary.core :as secretary
             :refer-macros [defroute]]))

;;
;; Hash tools
;;


(defn get-hash []
  (.. js/window -location -hash))


(defn set-hash [hash]
  (set! (.. js/window -location -hash) hash))


;;
;; Routes
;;

(defroute auth "/auth" []
  (rf/dispatch [:ui.events/page :auth]))


(defroute search "/search" {{q :q} :query-params}
  (rf/dispatch [:ui.events/page :search-feeds {:q q}]))


(defroute feed "/feeds/:id" [id]
  (rf/dispatch [:ui.events/page :feed {:feed-id (int id)}]))

;;
;; Goto functions
;;


(defn goto-search
  [q]
  (set-hash (search {:query-params {:q q}})))


(defn goto-auth
  []
  (set-hash (auth)))


;;
;; Dispatch and History
;;


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
  (init-history)
  (dispatch))
