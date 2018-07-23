(ns ui.core
  (:require [ui.views :as views]
            [ui.nav :as nav]
            [ui.scroll :as scroll]
            [ui.sidebar :as sidebar]
            [ui.auth :as auth]
            [ui.bar :as bar]
            [ui.search :refer (view-search-form)]

            [reagent.core :as r]
            [re-frame.core :as rf]))


(defn el-by-id [id]
  (.getElementById js/document id))


(defn init-mount []
  (r/render [bar/view-bar]            (el-by-id "bar"))
  (r/render [auth/view-user-block]    (el-by-id "user-block"))
  (r/render [view-search-form]        (el-by-id "search-block"))
  (r/render [sidebar/view-sidebar]    (el-by-id "sidebar-ui"))
  (r/render [views/view-page]         (el-by-id "page")))


(defn ^:export init
  []
  (nav/init)
  (scroll/init)
  (views/init)
  (init-mount))


(init)


;; (rf/dispatch [:ui.events/api.feeds])
