(ns ui.views
  (:require ui.events
            ui.subs
            [ui.search :refer (view-search-results)]
            [ui.time :as t]
            [ui.common :refer (js-stub get-feed-title get-feed-image)]
            [ui.auth :as auth]

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
       [arr-right]]]

     ]]]
  )

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
            {:ordering ordering}]))

        ]

    [:div.menu-items

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
     ;; Layout
     ;;

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
        {:keys [link]} feed]

    [:div#feed-header
     [:h1.overflow-split
      [:a
       {:href link}
       (get-feed-title feed)]]

     [:p "Варламов // by Ivan Grishaev // 1 Jun 2018"]

     [feed-controls feed]]))

(defn rf-partial
  [event & init]
  (fn [& more]
    (rf/dispatch
     (into (into [event] init) more))))

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


(defn get-entry-date
  [entry]
  (or (:date_published_at entry)
      (:date_updated_at entry)
      (:updated_at entry)
      (:created_at entry)))


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
                        index entry-id true]))

        ]

    [:div.entry

     ;; todo read style
     {:style (when is_read
               {:background-color "#000"})}

     (when auto_read
       [entry-scroll feed-id index])

     [:h2.overflow-split
      [:a {:href link} title]]

     [:p
      (get-feed-title feed)
      " // "
      author
      " // "
      (t/humanize entry-date)]

     [:div.menu-items

      [:div.menu-item
       [:a {:href js-stub}
        "Bookmark"]]

      [:div.menu-item
       [:a {:href js-stub
            :on-click #(api-mark-read true)}
        "Mark read"]]]

     [:div.entry-content.overflow-split
      {:dangerouslySetInnerHTML {:__html summary}}]

     [:div.entry-controls [:a {:href link} "Visit page →"]]]))


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
       [view-entry feed-id index])
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


(defn init
  []

  (rf/dispatch [:ui.events/api.user-info])


  )
