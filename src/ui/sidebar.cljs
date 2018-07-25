(ns ui.sidebar
  (:require [ui.common :refer (get-feed-title get-fav-url)]
            [ui.nav :as nav]

            [re-frame.core :as rf]))


(defn view-sync-button
  []
  [:button
   {:on-click
    #(rf/dispatch [:ui.events/api.feeds])}
   "Sync"])


(defn view-feed-list
  []
  (let [feeds @(rf/subscribe [:ui.subs/feeds])]

    [:div

     [view-sync-button]

     #_ ;; TODO show folders later
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
         [:a
          {:href (nav/feed {:id feed-id})}
          (get-feed-title feed)]]

        [:div.flex-separator]

        (when (>= unread 0)
          [:div [:span.feed-count unread]])])]))


(defn view-sidebar
  []
  (let [user @(rf/subscribe [:auth/user])]
    [:div
     (when user [view-feed-list])]))
