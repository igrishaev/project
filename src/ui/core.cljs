(ns ui.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]

            ;; [qrfd.events]
            ;; [qrfd.subs]
            ;; [qrfd.modal :as modal]
            ;; [qrfd.routes :as routes]
            ;; [qrfd.charts :as charts]
            ;; [qrfd.views :as views]
            )
  )

(defn get-by-id [id]
  (.getElementById js/document id))

#_
(defn init-mount []
  (r/render [views/view-top-navbar] (get-by-id "view-top-navbar"))
  (r/render [modal/view-modal] (get-by-id "view-modal"))
  (r/render [views/view-content] (get-by-id "app")))

#_
(defn init-db
  []
  (rf/dispatch [:qrfd.events/init-db]))

#_
(defn init-urls
  []
  (rf/dispatch [:qrfd.events/api.list_urls]))


(defn ^:export init []
  (js/alert "test2")
  ;; (init-db)
  ;; (routes/init)
  ;; (charts/init)
  ;; (init-mount)
  ;; (init-urls)
  )

(init)
