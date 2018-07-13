(ns ui.views
  (:require ui.events
            ui.subs
            [ui.url :as url]
            [ui.util :refer [clear-str]]

            [clojure.string :as str]

            [reagent.core :as r]
            [reagent-forms.core :refer [bind-fields]]
            [re-frame.core :as rf]))

(def js-stub "javascript:;")

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

(defn get-feed-image
  [feed]
  (or (:url_image feed)
      (get-fav-url feed)))

(defn view-preview
  []
  (when-let [feed @(rf/subscribe [:ui.subs/preview])]
    (let [{feed-id :id :keys [subtitle link]} feed]
      [:div#search-result-list
       [:div.search-result-item
        [:div.search-result-item-image
         [:img {:src (get-feed-image feed)}]]
        [:div.search-result-item-content
         [:h2 (get-feed-title feed nil)]
         [:p.subtitle subtitle]
         [:p.subtitle
          "55K followers | 279 articles per week | #tech #startups"]]
        [:div.search-result-item-actions
         [:a.action-vert.action-main-vert
          {:href js-stub
           :on-click
           #(rf/dispatch [:ui.events/api.subscribe feed-id])}
          "Follow"]
         [:a.action-vert
          {:href "#"}
          "Similar"]]]
       ])





    )





  )

(defn view-page
  []

  #_
  (rf/dispatch [:ui.events/api.subs])

  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page]

    (case page
      :sub [view-sub params]
      :preview [view-preview]
      ;; :index [page-dashboard]
      ;; :stats [page-hash params]
      ;; :profile [profile/page-profile params]

      nil)))
