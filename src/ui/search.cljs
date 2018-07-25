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


(defn view-search-results
  []

  (let [page @(rf/subscribe [:ui.subs/page])
        {:keys [page params]} page
        {:keys [q]} params]

    (rf/dispatch [:ui.events/api.search-feeds q])

    (when-let [feeds @(rf/subscribe [:ui.subs/search-feeds])]
      [:div#search-result-list
       (for [feed feeds
             :let [{feed-id :id
                    :keys [entries
                           subtitle
                           sub_count_total]} feed]]
         ^{:key feed-id}
         [:div.search-result-item
          [:div.search-result-item-image
           [:img {:src (get-feed-image feed)}]]
          [:div.search-result-item-content
           [:h2 {:dangerouslySetInnerHTML {:__html (get-feed-title feed)}}]
           [:p.subtitle {:dangerouslySetInnerHTML {:__html subtitle}}]

           ;; todo show actial data
           [:p.subtitle
            (pluralize "subscriber" sub_count_total)]

           #_
           (for [entry entries
                 :let [{entry-id :id :keys [title]} entry]]

             ^{:key entry-id}
             [:p.small title])]

          [:div.search-result-item-actions
           [:a.action-vert.action-main-vert
            {:href js-stub
             :on-click
             #(rf/dispatch [:ui.events/api.subscribe feed-id])}
            "Subscribe"]

           #_
           [:a.action-vert
            {:href js-stub}
            "Similar"]]])])))
