(ns ui.core
  (:require [ui.views :as views]
            [ui.routes :as routes]
            [ui.scroll :as scroll]

            [ui.bar :as bar]

            [reagent.core :as r]
            [re-frame.core :as rf]))

(defn el-by-id [id]
  (.getElementById js/document id))

(defn init-mount []
  #_
  (r/render [views/foobar] (el-by-id "app-content"))

  (r/render [bar/bar] (el-by-id "bar"))

  (r/render [views/view-page] (el-by-id "page"))
  (r/render [views/view-search-form] (el-by-id "search-block"))
  (r/render [views/left-sidebar] (el-by-id "sidebar-ui"))

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
  ;; (init-db)
  (routes/init)
  (init-mount)
  (scroll/init)
  ;; (init-urls)

  (rf/dispatch [:ui.events/api.feeds])

  )

(init)
