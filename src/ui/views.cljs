(ns ui.views
  (:require ui.events
            ui.subs
            [ui.time :as t]
            [ui.url :as url]
            [ui.util :refer [clear-str]]

            [clojure.string :as str]

            [goog.functions :refer [rateLimit]]

            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [re-frame.core :as rf]))

(def js-stub "javascript:;")

(def arr-down
  [:span {:dangerouslySetInnerHTML {:__html "&#9662"}}])

(def arr-right
  [:span {:dangerouslySetInnerHTML {:__html "&rarr;"}}])

;; todo move that

(defn view-sync-button
  []
  [:button
   {:on-click
    #(rf/dispatch [:ui.events/api.feeds])}
   "Sync"])


(defn get-feed-title
  [feed]
  (or (some-> feed :sub :title clear-str)
      (some-> feed :title clear-str)
      (some-> feed :subtitle clear-str)
      (-> feed :url_source url/get-short-url)))

(defn get-fav-url
  [feed]
  (let [{:keys [url_favicon url_source]} feed]
    (or url_favicon
        (url/get-fav-url url_source))))

(defn left-sidebar
  []
  (let [feeds @(rf/subscribe [:ui.subs/feeds])]

    [:div

     [view-sync-button]

     [:div.sidebar-folder
      [:div.sidebar-section.sidebar-feed.sidebar-tag
       "Design ▾"]]

     (for [feed feeds
           :let [feed-id (-> feed :id)
                 unread (-> feed :sub :message_count_unread)]]

       ^{:key feed-id}
       [:div.sidebar-section.sidebar-feed

        [:div.feed-image
         [:img {:src (get-fav-url feed)}]]

        [:div.feed-title.overflow-cut
         [:span

          #_
          {:href (str "#/subs/" sub-id)}

          {:on-click
           (fn [e]
             (rf/dispatch [:ui.events/api.messages feed-id])
             (rf/dispatch [:ui.events/page
                           :feed {:feed-id feed-id}]))}

          (get-feed-title feed)]]

        [:div.flex-separator]

        (when (pos? unread)
          [:div [:span.feed-count unread]])])]))

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

(defn read-more
  [feed-id]
  (let [node (atom nil)]

    (r/create-class

     {:component-did-mount
      (fn [this]
        (reset! node (r/dom-node this)))

      :reagent-render
      (fn []
        (let [{:keys [scroll height-viewport]}
              @(rf/subscribe [:scroll])

              entry @(rf/subscribe [:ui.subs/last-entry feed-id])
              {entry-id :id} entry

              loader @(rf/subscribe [:ui.subs/loader])
              {:keys [works is-empty]} loader

              event (rateLimit
                     (fn [] (rf/dispatch
                             [:ui.events/api.read-more
                              feed-id entry-id]))
                     2000)]

          (if-let [node @node]
            (let [offset (.. node -offsetTop)]
              (when (< offset (+ scroll height-viewport))
                (when-not works
                  (when-not is-empty
                    (event))))))

          (if works
            [:div.read-more
             [:div.load-wrapper
              [:div.loader]
              [:span "Loading..."]]]

            [:div])))})))

(defn entry-scroll
  [feed-id index]

  (let [node (atom nil)]

    (r/create-class

     {:component-did-mount
      (fn [this]
        (reset! node (r/dom-node this)))

      :reagent-render
      (fn []
        (let [entry
              @(rf/subscribe
                [:ui.subs/find-entry feed-id index])

              {entry-id :id} entry

              api-mark-read
              (fn [flag]
                (rf/dispatch [:ui.events/api.mark-read
                              index entry-id true]))

              is_read (-> entry :message :is_read)

              {:keys [scroll]}
              @(rf/subscribe [:scroll])]

          (when-let [node @node]
            (let [offset (.. node -offsetTop)]
              (when (and (not is_read)
                         (> scroll offset))
                (api-mark-read true)))))

        [:div])})))

(defn view-entry
  ;; todo track scroll in a separate view!
  [feed-id index]
  (let [entry @(rf/subscribe [:ui.subs/find-entry
                              feed-id index])

        feed @(rf/subscribe [:ui.subs/find-feed feed-id])
        {entry-id :id
         :keys [link title summary author]} entry
        is_read (-> entry :message :is_read)
        auto_read (-> feed :sub :auto_read)
        entry-date (or (:date_published_at entry)
                       (:date_updated_at entry)
                       (:updated_at entry)
                       (:created_at entry))

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
    [:div#feed-items
     (for [[index entry-id] entries]
       ^{:key entry-id}
       [view-entry feed-id index])
     [read-more feed-id]]))

(defn view-feed
  [params]
  (let [{:keys [feed-id]} params
        feed-id (int feed-id)]

    [:div
     [feed-header feed-id]
     [feed-entries feed-id]]))

(def form-search
  [:input#search-field
   {:placeholder "Unput a URL here"
    :type "text"
    :field :text
    :id :search
    :validator
    (fn [{:keys [search]}]
      (when (and search (clear-str search))
        (when-not (url/valid-url? search)
          ["invalid"])))}])

(defn view-search
  []
  (let [doc (r/atom {})
        handler
        (fn [e]
          (.preventDefault e)
          (let [{:keys [search]} @doc]
            (rf/dispatch [:ui.events/page :search-feeds {:tearm search}])
            (rf/dispatch [:ui.events/api.search-feeds search])))]
    (fn []
      [:div#search-block.menu-item
       [:form
        {:on-submit handler}
        [bind-fields form-search doc]
        [:button.btn-submit
         {:type "Button"
          :on-click handler} "Search"]]])))

(defn get-feed-image
  [feed]
  (or (:url_image feed)
      (get-fav-url feed)))

(defn view-feed-search
  []
  (when-let [feeds @(rf/subscribe [:ui.subs/search-feeds])]
    [:div#search-result-list
     (for [feed feeds
           :let [{feed-id :id
                  :keys [entries subtitle]} feed]]
       ^{:key feed-id}
       [:div.search-result-item
        [:div.search-result-item-image
         [:img {:src (get-feed-image feed)}]]
        [:div.search-result-item-content
         [:h2 (get-feed-title feed)]
         [:p.subtitle subtitle]
         [:p.subtitle
          "55K followers | 279 articles per week | #tech #startups"]

         (for [entry entries
               :let [{entry-id :id :keys [title]} entry]]

           ^{:key entry-id}
           [:p.small title])]

        [:div.search-result-item-actions
         [:a.action-vert.action-main-vert
          {:href js-stub
           :on-click
           #(rf/dispatch [:ui.events/api.subscribe feed-id])}
          "Follow"]
         [:a.action-vert
          {:href "#"}
          "Similar"]]])]))

(defn view-page
  []
  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page]

    (case page
      :feed [view-feed params]
      :search-feeds [view-feed-search]
      ;; :index [page-dashboard]
      ;; :stats [page-hash params]
      ;; :profile [profile/page-profile params]

      nil)))
