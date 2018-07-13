(ns ui.views
  (:require ui.events
            ui.subs
            [ui.url :as url]
            [ui.util :refer [clear-str]]

            [clojure.string :as str]

            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [re-frame.core :as rf]))


;; todo move that

(defn view-sync-button
  []
  [:button
   {:on-click
    #(rf/dispatch [:ui.events/api.subs])}
   "Sync"])


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
    [:div

     [:div.sidebar-folder
      [:div.sidebar-section.sidebar-feed.sidebar-tag
       "Design ▾"]]

     (for [{:keys [feed sub]} subs
           :let [{sub-id :id :keys [message_count_unread]} sub]]

       ^{:key sub-id}
       [:div.sidebar-section.sidebar-feed
        [:div.feed-image
         [:img {:src (get-fav-url feed)}]]
        [:div.feed-title.overflow-cut
         [:span

          #_
          {:href (str "#/subs/" sub-id)}

          {:on-click
           (fn [e]
             (rf/dispatch [:ui.events/api.messages sub-id])
             (rf/dispatch [:ui.events/page :sub {:sub-id sub-id}]))}

          (get-feed-title feed sub)]]
        [:div.flex-separator]
        (when (pos? message_count_unread)
          [:div [:span.feed-count message_count_unread]])])]))

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
)

(def js-stub "javascript:;")

(defn feed-controls
  [feed sub]

  [:div.menu-items
   [:div.dropdown.menu-item
    [:a.dropbtn {:href "#"} "Order by ▾"]
    [:div.dropdown-content
     [:a {:href "fff"} "Link 1"]
     [:a {:href "aaa"} "Link 2"]
     [:a {:href "ccc"} "Link 3"]]]

   [:div.dropdown.menu-item
    [:a.dropbtn {:href "#"}
     "Layout " [:span {:dangerouslySetInnerHTML {:__html "&#9662"}}]]

    [:div.dropdown-content
     [:a {:href js-stub
          :on-click

          (fn [e]
            (prn "123"))
          }
      "Full article"]

     [:a {:href "#"}
      "Titles only"]]]

   [:div.menu-item [:a {:href "#"} "Edit"]]
   [:div.menu-item [:a {:href "#"} "Unsubscribe!"]]]

  )

(defn feed-header
  [feed sub]

  [:div#feed-header
   [:h1.overflow-split
    [:a
     {:href (:link feed)}
     (get-feed-title feed sub)]]

   [:p "Варламов // by Ivan Grishaev // 1 Jun 2018"]

   [feed-controls feed sub]])

(defn feed-entries
  [msgs]
  [:div#feed-items

   (for [{:keys [entry message]} msgs
         :let [{entry-id :id :keys [link summary title]} entry]]

     ^{:key entry-id}
     [:div.entry.overflow-split

      [:h2.overflow-split
       [:a {:href link} title]]

      [:p "Варламов // by Ivan Grishaev // 1 Jun 2018\n\n     "]

      [:div.menu-items
       [:div.dropdown.menu-item
        [:a.dropbtn {:href "#"} "Order by ▾"]
        [:div.dropdown-content
         [:a {:href "fff"} "Link 1"]
         [:a {:href "aaa"} "Link 2"]
         [:a {:href "ccc"} "Link 3"]]]

       [:div.dropdown.menu-item
        [:a.dropbtn {:href "#"} "Layout ▾"]
        [:div.dropdown-content
         [:a {:href "fff"} "Link 1"]
         [:a {:href "aaa"} "Link 2"]
         [:a {:href "ccc"} "Link 3"]]]

       [:div.menu-item [:a {:href "#"} "Star"]]
       [:div.menu-item [:a {:href "#"} "Bookmark"]]
       [:div.menu-item [:a {:href "#"} "Hide"]]]

      [:div.entry-content.overflow-split
       {:dangerouslySetInnerHTML {:__html summary}}

       ]
      [:div.entry-controls [:a {:href link} "Visit page →"]]]

     )

   ])


(defn view-sub
  [params]
  (let [{:keys [sub-id]} params
        sub-id (int sub-id)]

    #_
    (rf/dispatch [:ui.events/api.messages sub-id])

    (let [msgs @(rf/subscribe [:ui.subs/messages sub-id])

          sub @(rf/subscribe [:ui.subs/find-sub sub-id])
          {:keys [sub feed]} sub

          ]

      [:div
       [feed-header feed sub]
       [feed-entries msgs]


       ]



      #_
      [:div.main-content.container-fluid



       (for [{:keys [entry message]} msgs
             :let [{msg-id :id} message]]

         ^{:key msg-id}
         [view-message entry message])

       ]))

  )

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
        handler (fn [e]
                  (.preventDefault e)
                  (let [{:keys [search]} @doc]
                    (rf/dispatch [:ui.events/api.preview search])))]
    (fn []
      [:div#search-block.menu-item
       [:form
        {:on-submit handler}
        [bind-fields form-search doc]
        [:button.btn-submit
         {:type "Button"
          :on-click handler} "Search"]]])))

(defn view-preview
  []
  [:div.col-12.col-lg-6
    [:div.card
     [:div.card-header "Latest Activity"]
     [:div.card-body
      [:ul.user-timeline.user-timeline-compact
       [:li.latest
        [:div.user-timeline-date "Just Now"]
        [:div.user-timeline-title "Create New Page"]
        [:div.user-timeline-description
         "Vestibulum lectus nulla, maximus in eros non, tristique."]]
       [:li
        [:div.user-timeline-date "Today - 15:35"]
        [:div.user-timeline-title "Back Up Theme"]
        [:div.user-timeline-description
         "Vestibulum lectus nulla, maximus in eros non, tristique."]]
       [:li
        [:div.user-timeline-date "Yesterday - 10:41"]
        [:div.user-timeline-title "Changes In The Structure"]
        [:div.user-timeline-description
         "Vestibulum lectus nulla, maximus in eros non, tristique.      "]]
       [:li
        [:div.user-timeline-date "Yesterday - 3:02"]
        [:div.user-timeline-title "Fix the Sidebar"]
        [:div.user-timeline-description
         "Vestibulum lectus nulla, maximus in eros non, tristique."]]]]]]



  #_
  [:div {:class "page-head"}
   [:h2 {:class "page-head-title"}
    "test"]

   #_
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

      [:div {:class "dropdown-menu" :role "menu"}
       [:a {:class "dropdown-item" :href "#"} "Newest"]
       [:a {:class "dropdown-item" :href "#"} "Oldest"]]]

     [:div.btn-group.btn-space
      [:a.btn.btn-secondary.dropdown-toggle
       {:type "button" :data-toggle "dropdown" :aria-expanded "false"}
       "Layout "
       [:span {:class "icon-dropdown mdi mdi-chevron-down"}]]

      [:div {:class "dropdown-menu" :role "menu"}
       [:a {:class "dropdown-item" :href "#"} "Full article"]
       [:a {:class "dropdown-item" :href "#"} "Only titles"]
       [:a {:class "dropdown-item" :href "#"} "Cards"]]]

     [:div.btn-group.btn-space
      [:a.btn.btn-secondary.dropdown-toggle
       {:type "button" :data-toggle "dropdown" :aria-expanded "false"}
       "Filters "
       [:span {:class "icon-dropdown mdi mdi-chevron-down"}]]

      [:div {:class "dropdown-menu" :role "menu"}
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
     [:li {:class "breadcrumb-item active"} "Buttons"]]]]


  )

(defn view-page
  []

  #_
  (rf/dispatch [:ui.events/api.subs])

  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page]

    (case page
      :sub [view-sub params]
      ;; :preview [view-preview]
      ;; :index [page-dashboard]
      ;; :stats [page-hash params]
      ;; :profile [profile/page-profile params]

      nil)))
