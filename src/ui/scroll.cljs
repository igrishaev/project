(ns ui.scroll
  (:require [goog.functions :refer [debounce]]

            [re-frame.core :as rf]))


(rf/reg-event-db
 :scroll
 (fn [db [_ params]]
   (assoc db :scroll params)))


(rf/reg-sub
 :scroll
 (fn [db [_]]
   (get db :scroll)))


(defn on-scroll []
  (let [height-full js/document.body.scrollHeight
        height-viewport js/window.innerHeight
        scroll (or js/window.pageYOffset
                   js/document.documentElement.scrollTop
                   js/document.body.scrollTop)]

    (rf/dispatch [:scroll
                  {:height-full height-full
                   :height-viewport height-viewport
                   :scroll scroll}])))


(def scroll-delta 300)


(defn init
  []
  (js/window.addEventListener
   "scroll"
   (goog.functions/debounce on-scroll scroll-delta)))

#_
(defn foo
  []

  (let [
        full_height js/document.body.scrollHeight
        viewport_height js/window.innerHeight

        scrolled (or js/window.pageYOffset
                     js/document.documentElement.scrollTop
                     js/document.body.scrollTop)

        delta 200

        scrolled_bottom (+ scrolled viewport_height)
        at_bottom (> scrolled_bottom (- full_height delta))]

    (when at_bottom
      (js/console.log "!!!"))))
