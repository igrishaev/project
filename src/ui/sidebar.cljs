(ns ui.sidebar
  (:require [ui.common :refer (js-stub
                               get-feed-title
                               get-fav-url)]
            [ui.nav :as nav]
            [ui.util :refer (format pluralize)]
            [ui.time :refer (humanize)]
            [ui.opml :as opml]

            [re-frame.core :as rf]))


(defn view-feed-header
  []
  (let [feeds @(rf/subscribe [:ui.subs/feeds])
        user @(rf/subscribe [:auth/user])]
    [:div#sidebar-feed-header

     [opml/import-button]

     [:p
      (or (:name user) (:email user))
      [:br]
      [:small
       (pluralize "feed" (count feeds))
       " // "
       "Last update "
       (humanize (:sync_date_last user))]]

     [:div.menu-items.controls
      [:div.dropdown.menu-item
       [:a
        {:href js-stub
         :on-click #(rf/dispatch [:ui.events/api.feeds])}
        "Refresh"]]]]))

(def no-label
  "Other")

(defn view-feed-list
  []
  (let [feeds @(rf/subscribe [:ui.subs/feeds])

        groups (group-by
                (fn [f] (-> f :sub :label))
                (filter :sub feeds))]

    [:div

     (for [label (-> groups keys sort)]

       [:div {:key (or label "")}

        [:div.sidebar-folder
         [:div.sidebar-section.sidebar-feed.sidebar-tag

          (or label no-label)]]

        (for [feed (reverse (get groups label))

              :let [feed-id (-> feed :id)
                    unread (-> feed :sub :message_count_unread)]]

          [:div.sidebar-section.sidebar-feed
           {:key feed-id}

           [:div.feed-image
            [:img {:src (get-fav-url feed)}]]

           [:div.feed-title.overflow-cut
            [:a
             {:href (nav/feed {:id feed-id})
              :dangerouslySetInnerHTML {:__html (get-feed-title feed)}}]]

           [:div.flex-separator]

           (when (>= unread 0)
             [:div [:span.feed-count unread]])])])]))


(defn view-sidebar
  []
  (let [user @(rf/subscribe [:auth/user])]
    (when user
      [:div
       [view-feed-header]
       [view-feed-list]])))
