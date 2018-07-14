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
             (rf/dispatch [:ui.events/page :feed {:feed-id feed-id}]))}

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

     [:div.card-footer.card-footer-contra__.text-center__
      [:a.card-link {:href (:link entry)}
       "Visit the page "
       [:span {:dangerouslySetInnerHTML {:__html "&rarr;"}}]]]


]]]
)

(defn feed-controls
  [feed]

  [:div.menu-items
   [:div.dropdown.menu-item
    [:a.dropbtn {:href "#"} "Order by ▾"]
    [:div.dropdown-content
     [:a {:href "fff"} "Link 1"]
     [:a {:href "aaa"} "Link 2"]
     [:a {:href "ccc"} "Link 3"]]]

   [:div.dropdown.menu-item
    [:a.dropbtn {:href "#"}
     "Layout "
     [:span {:dangerouslySetInnerHTML {:__html "&#9662"}}]]

    [:div.dropdown-content
     [:a {:href js-stub
          :on-click
          (fn [e]
            (js/alert "todo"))
          }
      "Full article"]

     [:a {:href "#"}
      "Titles only"]]]

   [:div.menu-item [:a {:href "#"} "Edit"]]
   [:div.menu-item [:a {:href "#"} "Unsubscribe!"]]]

  )

(defn feed-header
  [feed]

  (let [{:keys [link]} feed]


    [:div#feed-header
     [:h1.overflow-split
      [:a
       {:href link}
       (get-feed-title feed)]]

     [:p "Варламов // by Ivan Grishaev // 1 Jun 2018"]

     [feed-controls feed]]))

(defn feed-entries
  [entries]
  [:div#feed-items

   (for [entry entries
         :let [{entry-id :id
                :keys [link summary title]} entry]]

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


(defn view-feed
  [params]
  (let [{:keys [feed-id]} params
        feed-id (int feed-id)

        entries @(rf/subscribe [:ui.subs/entries feed-id])
        {:keys [feed_id entries]} entries
        feed @(rf/subscribe [:ui.subs/find-feed feed_id])]

    [:div
     [feed-header feed]
     [feed-entries entries]]))

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
         [:h2 (get-feed-title feed)]
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
  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page]

    (case page
      :feed [view-feed params]
      :preview [view-preview]
      ;; :index [page-dashboard]
      ;; :stats [page-hash params]
      ;; :profile [profile/page-profile params]

      nil)))
