(ns ui.views
  (:require ui.events
            ui.subs
            [ui.search :refer (view-search-results)]
            [ui.time :as t]
            [ui.common :refer (js-stub get-feed-title get-feed-image rf-partial)]
            [ui.auth :as auth]
            [ui.entry :as entry]

            cljsjs.pluralize

            [clojure.string :as str]
            [goog.functions :refer [rateLimit]]

            [reagent.core :as r]
            [re-frame.core :as rf]))


(def arr-down
  [:span {:dangerouslySetInnerHTML {:__html "&#9662"}}])

(def arr-right
  [:span {:dangerouslySetInnerHTML {:__html "&rarr;"}}])


(defn view-message
  [entry message]

  [:div.row
   [:div.col-12.col-lg-9.offset-lg-1
    [:div.card.app-message
     [:div.card-header.card-header-divider

      (if-let [link (:link entry)]
        [:a.title
         {:href link :target :_blank}
         (:title entry)]

        [:span.title
         (:title entry)])

      [:span.card-subtitle "Card subtitle description"]]

     [:div.card-body
      [:div.app-message-summary
       {:dangerouslySetInnerHTML {:__html (:summary entry)}}]

      ;;btn.btn-primary
      ]

     [:div.card-footer
      [:a.card-link {:href (:link entry)}
       "Visit the page "
       [arr-right]]]]]])


(defn feed-controls
  [feed]
  (let [{feed-id :id} feed

        api-layout
        (fn [layout]
          (rf/dispatch
           [:ui.events/api.update-subscription
            feed-id
            {:layout layout}]))

        api-mark-read
        (fn [flag]
          (rf/dispatch
           [:ui.events/api.update-subscription
            feed-id
            {:auto_read flag}]))

        api-unread-only
        (fn [flag]
          (rf/dispatch
           [:ui.events/api.update-subscription
            feed-id
            {:unread_only flag}]))

        api-ordering
        (fn [ordering]
          (rf/dispatch
           [:ui.events/api.update-subscription
            feed-id
            {:ordering ordering}]))]

    [:div.menu-items.controls

     ;;
     ;; Auto read
     ;;

     [:div.dropdown.menu-item
      [:a.dropbtn {:href js-stub}
       "Auto mark read " arr-down]
      [:div.dropdown-content
       [:a {:href js-stub
            :on-click #(api-mark-read true)}
        "On scroll"]

       [:a {:href js-stub
            :on-click #(api-mark-read false)}
        "Off"]]]

     ;;
     ;; Ordering
     ;;

     [:div.dropdown.menu-item
      [:a.dropbtn {:href js-stub} "Order by " arr-down]
      [:div.dropdown-content

       [:a {:href js-stub
            :on-click #(api-ordering "new_first")}
        "Newest first"]

       [:a {:href js-stub
            :on-click #(api-ordering "old_first")}
        "Oldest first"]]]

     ;;
     ;; Unread only
     ;;

     [:div.dropdown.menu-item
      [:a.dropbtn {:href js-stub} "Unread only " arr-down]
      [:div.dropdown-content

       [:a {:href js-stub
            :on-click #(api-unread-only true)}
        "True"]

       [:a {:href js-stub
            :on-click #(api-unread-only false)}
        "False"]]]

     ;;
     ;; Layout TODO implement
     ;;

     #_
     [:div.dropdown.menu-item
      [:a.dropbtn {:href js-stub}
       "Layout " arr-down]

      [:div.dropdown-content

       [:a {:href js-stub
            :on-click #(api-layout "full_article")}
        "Full article"]

       [:a {:href js-stub
            :on-click #(api-layout "titles_only")}
        "Titles only"]

       [:a {:href js-stub
            :on-click #(api-layout "cards")}
        "Cards"]]]

     ;;
     ;; Refresh
     ;;

     [:div.menu-item
      [:a {:href js-stub
           :on-click
           #(rf/dispatch [:ui.events/api.messages feed-id])}
       "Refresh"]]

     [:div.menu-item
      [:a {:href js-stub
           :on-click
           #(rf/dispatch [:ui.events/api.unsubscribe feed-id])}
       "Unsubscribe"]]]))


(defn feed-header
  [feed-id]

  (let [feed @(rf/subscribe [:ui.subs/find-feed feed-id])
        {:keys [link date_updated_at sub_count_total]} feed]

    [:div#feed-header
     [:h1.overflow-split
      [:a
       {:href link}
       (get-feed-title feed)]]

     [:p.subinfo
      (js/pluralize "subscriber" sub_count_total true)

      " // updated "
      (t/humanize date_updated_at)]

     [feed-controls feed]]))


(defn read-more
  [feed-id]

  (let [node (atom nil)
        delta 1000
        trigger (rateLimit
                 (rf-partial :ui.events/api.read-more)
                 delta)]

    (r/create-class

     {:component-did-mount
      (fn [this]
        (reset! node (r/dom-node this)))

      :reagent-render
      (fn [feed-id]
        (let [{:keys [scroll height-viewport]}
              @(rf/subscribe [:scroll])

              entry @(rf/subscribe [:ui.subs/last-entry feed-id])
              {entry-id :id} entry

              loader @(rf/subscribe [:ui.subs/loader])]

          (if-let [node @node]
            (let [offset (.. node -offsetTop)]
              (when (< offset (+ scroll height-viewport))
                (when-not loader
                  (trigger feed-id entry-id)))))

          (if loader
            [:div.read-more
             [:div.load-wrapper
              [:div.loader]
              [:span "Loading..."]]]

            [:div])))})))


(defn feed-entries
  [feed-id]
  (let [entries @(rf/subscribe [:ui.subs/entries feed-id])]

    (when (empty? entries)
      ;; todo show message
      )

    [:div#feed-items
     (for [pair entries
           :let [[index entry-id] pair]]
       ^{:key pair}
       [entry/view-entry feed-id index])
     [read-more feed-id]]))


(defn view-feed
  [params]

  (let [{:keys [feed-id]} params
        feed-id (int feed-id)]

    (rf/dispatch [:ui.events/api.messages feed-id])

    [:div
     [feed-header feed-id]
     [feed-entries feed-id]]))


(defn view-page
  []
  (if-let [user @(rf/subscribe [:auth/user])]
    (let [page @(rf/subscribe [:ui.subs/page])
          {:keys [page params]} page]
      (case page
        :feed         [view-feed params]
        :search-feeds [view-search-results]
        nil))
    [auth/view-auth]))
