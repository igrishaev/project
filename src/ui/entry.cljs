(ns ui.entry
  (:require [ui.time :as t]

            [ui.common :refer (js-stub get-feed-title get-entry-date rf-partial)]

            [clojure.string :as str]

            [reagent.core :as r]
            [re-frame.core :as rf]))


(defn entry-scroll
  [feed-id index]

  (let [node (atom nil)
        trigger (rf-partial :ui.events/api.mark-read)]

    (r/create-class

     {:component-did-mount
      (fn [this]
        (reset! node (r/dom-node this)))

      :reagent-render
      (fn [feed-id index]
        (let [entry
              @(rf/subscribe
                [:ui.subs/find-entry feed-id index])

              {entry-id :id} entry

              is_read (-> entry :message :is_read)

              {:keys [scroll]}
              @(rf/subscribe [:scroll])]

          (when-let [node @node]
            (let [offset (.. node -offsetTop)
                  mark? (and (not is_read)
                             (> scroll offset))]
              (when mark?
                (trigger index entry-id true)
                (rf/dispatch [:ui.events/feed-read-count-dec
                              feed-id])))))

        [:div])})))


(defn mime->kw
  [mime]
  (some-> mime str/lower-case (str/split #"/" 2) first keyword))


(defn entry-enclosure
  [entry]
  (let [{:keys [enclosure_url enclosure_mime]} entry
        mime (mime->kw enclosure_mime)]

    [:div.entry-enclosure
     (case mime

       :audio
       [:audio {:controls true}
        [:source {:src enclosure_url
                  :type enclosure_mime}]]

       :video
       [:video {:controls true
                :width 640
                :height 480}
        [:source {:src enclosure_url
                  :type enclosure_mime}]]

       :image
       [:img {:src enclosure_url}]

       nil)]))


(defn view-entry
  [feed-id index]
  (let [entry @(rf/subscribe [:ui.subs/find-entry
                              feed-id index])

        feed @(rf/subscribe [:ui.subs/find-feed feed-id])

        {entry-id :id
         :keys [link title summary author]} entry

        is_read (-> entry :message :is_read)
        auto_read (-> feed :sub :auto_read)

        entry-date (get-entry-date entry)

        api-mark-read
        (fn [flag]
          (rf/dispatch [:ui.events/api.mark-read
                        index entry-id true]))]

    [:div.entry

     (when is_read
       {:class "is_read"})

     (when auto_read
       [entry-scroll feed-id index])

     [:h2.overflow-split
      [:a {:href link
           :dangerouslySetInnerHTML {:__html title}}]]

     [:p.subinfo

      [:span {:dangerouslySetInnerHTML {:__html (get-feed-title feed)}}]

      (when author
        (str " // " author))

      " // "

      [:span {:title entry-date}
       (t/humanize entry-date)]]

     [:div.menu-items.controls

      [:div.menu-item
       [:a {:href js-stub
            :on-click #(api-mark-read true)}
        "Mark read"]]]

     [:div.entry-content.overflow-split

      [entry-enclosure entry]

      [:div {:dangerouslySetInnerHTML {:__html summary}}]]

     [:div.entry-controls [:a {:href link} "Visit page →"]]]))
