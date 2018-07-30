(ns ui.search
  (:require [ui.common :refer (js-stub get-feed-title get-feed-image)]
            [ui.nav :as nav]
            [ui.util :refer (pluralize)]

            [clojure.string :as str]

            [reagent.core :as r]
            [re-frame.core :as rf]
            [reagent-forms.core :refer [bind-fields]]))


(def form-search
  [:input#search-field
   {:placeholder "Feed URL, web page, an term"
    :type "text"
    :field :text
    :id :search}])


(defn view-search-form
  []
  (let [*user (rf/subscribe [:auth/user])

        doc (r/atom {})

        handler
        (fn [e]
          (.preventDefault e)
          (let [{:keys [search]} @doc]
            (if (some-> search identity str/triml not-empty)
              (nav/goto-search search)
              (rf/dispatch [:bar/error "Search term cannot be blank"]))))]

    (fn []
      (when @*user
        [:div#search-block.menu-item
         [:form
          {:on-submit handler}
          [bind-fields form-search doc]
          [:button.btn-submit
           {:type "Button"
            :on-click handler} "Search"]]]))))


(defn subscribe-button
  [feed-id]

  (let [feed-dict @(rf/subscribe [:ui.subs/feed-dict])]

    (if (get-in feed-dict [feed-id :sub])

      [:a.action-vert.action-main-vert
       {:href js-stub
        :on-click
        #(rf/dispatch [:ui.events/api.unsubscribe feed-id])}
       "Unsubscribe"]

      [:a.action-vert.action-main-vert
       {:href js-stub
        :on-click
        #(rf/dispatch [:ui.events/api.subscribe feed-id])}
       "Subscribe"])))


(defn view-search-results
  []

  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page
        {:keys [q]} params]

    (rf/dispatch [:ui.events/api.search-feeds q])

    (when-let [feeds-search @(rf/subscribe [:ui.subs/search-feeds])]
      [:div#search-result-list

       (for [feed feeds-search
             :let [{feed-id :id
                    :keys [entries
                           subtitle
                           sub_count_total]} feed]]
         ^{:key feed-id}
         [:div.search-result-item
          [:div.search-result-item-image
           [:img {:src (get-feed-image feed)}]]
          [:div.search-result-item-content
           [:h2
            [:a
             {:href (nav/feed {:id feed-id})
              :dangerouslySetInnerHTML {:__html (get-feed-title feed)}}]]
           [:p.subtitle {:dangerouslySetInnerHTML {:__html subtitle}}]

           [:p.subtitle
            (pluralize "subscriber" sub_count_total)]

           #_
           (for [entry entries
                 :let [{entry-id :id :keys [title]} entry]]

             ^{:key entry-id}
             [:p.small title])]

          [:div.search-result-item-actions
           [subscribe-button feed-id]

           #_
           [:a.action-vert
            {:href js-stub}
            "Similar"]]])])))
