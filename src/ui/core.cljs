(ns ui.core
  (:require [ui.views :as views]
            [ui.routes :as routes]

            [reagent.core :as r]
            [re-frame.core :as rf]

            ;; [qrfd.events]
            ;; [qrfd.subs]
            ;; [qrfd.modal :as modal]

            ;; [qrfd.charts :as charts]
            ;; [qrfd.views :as views]
            )
  )

(defn el-by-id [id]
  (.getElementById js/document id))


(defn init-mount []
  ;; (r/render [views/view-top-navbar] (el-by-id "view-top-navbar"))
  ;; (r/render [modal/view-modal] (el-by-id "view-modal"))

  #_
  (r/render [views/foobar] (el-by-id "app-content"))

  (r/render [views/view-content] (el-by-id "app-content"))

  (r/render [views/view-search] (el-by-id "app-search-container"))

  (r/render [views/left-sidebar] (el-by-id "app-left-sidebar"))




  )

#_
(defn init-db
  []
  (rf/dispatch [:qrfd.events/init-db]))

#_
(defn init-urls
  []
  (rf/dispatch [:qrfd.events/api.list_urls]))


(defn ^:export init []
  #_
  (js/alert "test2")

  ;; (init-db)
  (routes/init)
  ;; (charts/init)
  (init-mount)
  ;; (init-urls)
  )

(init)
