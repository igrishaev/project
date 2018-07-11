(ns ui.views
  (:require ui.events
            ui.subs
            [ui.url :as url]

            [clojure.string :as str]
            [re-frame.core :as rf]))


;; todo move that

(defn view-sync-button
  []
  [:button
   {:on-click
    #(rf/dispatch [:ui.events/api.subs])}
   "Sync"])

(def clear-str (comp not-empty str/trim))

(defn get-feed-title
  [feed sub]
  (or (some-> sub :title clear-str)
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
  (let [subs @(rf/subscribe [:ui.subs/subs])]
    [:div.left-sidebar-wrapper
     [:a.left-sidebar-toggle {:href "#"} "Fixed Sidebar"]
     [:div.left-sidebar-spacer
      [:div.left-sidebar-scroll.ps-container.ps-theme-default.ps-active-y
       [:div.left-sidebar-content

        [view-sync-button]

        [:ul.sidebar-elements

         #_
         [:li.divider "Menu"]
         [:li
          [:a
           {:href "index.html"}
           [:i.icon.mdi.mdi-home]
           [:span "Dashboard"]]]

         [:li.parent.open
          [:a {:href "#"} [:i.icon.mdi.mdi-face] [:span "Feeds"]]
          [:ul.sub-menu

           (for [{:keys [feed sub]} subs
                 :let [{sub-id :id :keys [message_count_unread]} sub]]

             ^{:key sub-id}
             [:li [:a {:href (str "#/subs/" sub-id)}

                   [:img {:style {:margin-right :10px}
                          :src (get-fav-url feed)}]

                   (get-feed-title feed sub)

                   (when (pos? message_count_unread)
                     [:span.badge.float-right
                      message_count_unread])

                   ]])

           #_
           [:li
            [:a
             {:href "ui-cards.html"}
             [:span.badge.badge-primary.float-right "New"]
             "Cards"]]

]]]]]]]
    )

  )

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

     [:div.card-footer.card-footer-contra__.text-center__
      [:a.card-link {:href (:link entry)}
       "Visit the page "
       [:span {:dangerouslySetInnerHTML {:__html "&rarr;"}}]]]


]]]


  #_
  [:div.row
   [:div.col-lg-6

    [:div.card.card-contrast
     [:div.card-header ;; .card-header-contrast

      [:span {:dangerouslySetInnerHTML {:__html (:title entry)}}]

      [:span.card-subtitle "Card subtitle description"]]

     [:div.card-body
      [:div {:dangerouslySetInnerHTML {:__html (:summary entry)}}]]]]]
  )

(defn feed-header
  [feed sub]

  [:div {:class "page-head"}
   [:h2 {:class "page-head-title"}
    (get-feed-title feed sub)]

   (when-let [subtitle (:subtitle feed)]
     [:p.display-description
      {:style {:margin-bottom 0}}
      subtitle])

   [:div {:class "mt-4 mb-2"}
    [:div {:class "btn-toolbar"}

     [:div.btn-group.btn-space
      [:a.btn.btn-secondary.dropdown-toggle
       {:type "button" :data-toggle "dropdown" :aria-expanded "false"}
       "Order "
       [:span {:class "icon-dropdown mdi mdi-chevron-down"}]]

      [:div {:class "dropdown-menu" :role "menu" :x-placement "bottom-start"}
       [:a {:class "dropdown-item" :href "#"} "Newest"]
       [:a {:class "dropdown-item" :href "#"} "Oldest"]]]

     [:div.btn-group.btn-space
      [:a.btn.btn-secondary.dropdown-toggle
       {:type "button" :data-toggle "dropdown" :aria-expanded "false"}
       "Layout "
       [:span {:class "icon-dropdown mdi mdi-chevron-down"}]]

      [:div {:class "dropdown-menu" :role "menu" :x-placement "bottom-start"}
       [:a {:class "dropdown-item" :href "#"} "Full article"]
       [:a {:class "dropdown-item" :href "#"} "Only titles"]
       [:a {:class "dropdown-item" :href "#"} "Cards"]]]

     [:div.btn-group.btn-space
      [:a.btn.btn-secondary.dropdown-toggle
       {:type "button" :data-toggle "dropdown" :aria-expanded "false"}
       "Filters "
       [:span {:class "icon-dropdown mdi mdi-chevron-down"}]]

      [:div {:class "dropdown-menu" :role "menu" :x-placement "bottom-start"}
       [:a {:class "dropdown-item" :href "#"} "All"]
       [:a {:class "dropdown-item" :href "#"} "Unread"]]]

     #_
     [:div {:class "btn-group btn-space float-right"}
      [:a {:class "btn btn-secondary" :type "button"} "Unread only"]
      [:a {:class "btn btn-secondary" :type "button"} "Show all"]]

     #_
     [:div {:class "btn-group btn-space"}
      [:button {:class "btn btn-primary" :type "button"} "Left"]
      [:button {:class "btn btn-primary" :type "button"} "Mid"]
      [:button {:class "btn btn-primary" :type "button"} "Right"]]

     [:div {:class "btn-group btn-space"}
      [:a {:class "btn btn-secondary" :type "button"}
       "Update"]]

     [:div {:class "btn-group btn-space"}
      [:a {:class "btn btn-secondary" :type "button"}
       "Edit"]]

     [:div {:class "btn-group btn-space"}
      [:a {:class "btn btn-secondary" :type "button"}
       "Unsubscribe"]]

     #_
     [:div {:class "btn-group btn-space"}
      [:button {:class "btn btn-danger" :type "button"} "Left"]
      [:button {:class "btn btn-danger" :type "button"} "Mid"]
      [:button {:class "btn btn-danger" :type "button"} "Right"]]]]

   #_
   [:nav {:aria-label "breadcrumb" :role "navigation"}
    [:ol {:class "breadcrumb page-head-nav"}
     [:li {:class "breadcrumb-item"}
      [:a {:href "#"} "Home"]]
     [:li {:class "breadcrumb-item"}
      [:a {:href "#"} "UI Elements"]]
     [:li {:class "breadcrumb-item active"} "Buttons"]]]])

(defn bar
  []
  [:div.be-aside-header-filters
   [:div.be-aside-header-filters-left
    [:div.btn-group
     [:button.btn.btn-secondary.dropdown-toggle
      {:aria-expanded "false",
       :type "button",
       :data-toggle "dropdown"}
      "Order by "
      [:span.caret]]
     #_
     [:div.dropdown-menu
      {:style
       "position: absolute; transform: translate3d(0px, 30px, 0px); top: 0px; left: 0px; will-change: transform;",
       :x-placement "bottom-start",
       :role "menu"}
      [:a.dropdown-item {:href "#"} "Date"]
      [:a.dropdown-item {:href "#"} "Price (lower to higher)"]
      [:a.dropdown-item {:href "#"} "Price (higher to lower)"]
      [:a.dropdown-item {:href "#"} "Items"]]]
    [:div.btn-group
     [:button.btn.btn-secondary
      {:type "button"}
      [:i.icon.mdi.mdi-view-list]]
     [:button.btn.btn-secondary
      {:type "button"}
      [:i.icon.mdi.mdi-view-module]]]]
   [:div.be-aside-header-filters-right
    [:span.be-aside-pagination-indicator "1-50 of 253"]
    [:div.btn-group.be-aside-pagination-nav
     [:button.btn.btn-secondary
      {:type "button"}
      [:i.mdi.mdi-chevron-left]]
     [:button.btn.btn-secondary
      {:type "button"}
      [:i.mdi.mdi-chevron-right]]]]])


(defn view-sub
  [params]
  (let [{:keys [sub-id]} params
        sub-id (int sub-id)]

    (rf/dispatch [:ui.events/api.messages sub-id])
    (let [msgs @(rf/subscribe [:ui.subs/messages sub-id])

          sub @(rf/subscribe [:ui.subs/find-sub sub-id])
          {:keys [sub feed]} sub
          ]

      [:div.main-content.container-fluid

       [feed-header feed sub]

       (for [{:keys [entry message]} msgs
             :let [{msg-id :id} message]]

         ^{:key msg-id}
         [view-message entry message])

       ]))

  )



(defn view-content
  []

  (rf/dispatch [:ui.events/api.subs])

  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page]
    (case page
      :sub [view-sub params]
      ;; :index [page-dashboard]
      ;; :stats [page-hash params]
      ;; :profile [profile/page-profile params]

      nil)))
