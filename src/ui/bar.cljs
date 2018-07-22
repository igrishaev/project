(ns ui.bar
  (:require [reagent.core :as r]
            [re-frame.core :as rf]))

;; TODO clear by timeout

(rf/reg-event-db
 :bar/bar
 (fn [db [_ bar]]
   (-> db
       (assoc :bar bar))))


(rf/reg-event-fx
 :bar/error
 (fn [_ [_ text]]
   {:dispatch [:bar/bar {:text text :type :error}]}))


(rf/reg-event-fx
 :bar/info
 (fn [_ [_ text]]
   {:dispatch [:bar/bar {:text text :type :info}]}))


(rf/reg-event-fx
 :bar/clear
 (fn [_ [_]]
   {:dispatch [:bar/bar nil]}))


(rf/reg-sub
 :bar/bar
 (fn [db [_]]
   (get db :bar)))


(def clear-timeout
  (* 1000 5))

(defn bar
  []
  (when-let [bar @(rf/subscribe [:bar/bar])]
    (let [{:keys [text type]} bar]
      [:div
       {:class (str "bar bar-" (name type))}
       [:p
        [:span.bar-close
         {:dangerouslySetInnerHTML {:__html "&times;"}
          :on-click #(rf/dispatch [:bar/clear])}]
        text]])))
