(ns ui.events
  (:require [ui.db :as db]

            [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [ajax.core :as ajax]))

;;
;; Navigation
;;

(rf/reg-event-db
 ::page
 (fn [db [_ page params]]
   (assoc db :page {:page page :params params})))

;;
;; Api base
;;

(rf/reg-event-fx
 ::api.call
 (fn [_ [_ action params event-ok & [event-err]]]
   {:http-xhrio {:method :post
                 :uri "/api"
                 :format (ajax/json-request-format)
                 :params (merge params {:action action})
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [event-ok]
                 :on-failure [(or event-err ::api.error)]}}))

(rf/reg-event-fx
 ::api.error
 (fn [_ [& args]]
   (js/console.log args #_(clj->js data))

   nil
   #_
   {:dispatch-n
    [[::notification
      {:title "Error"
       :text (or (some-> data :response :error)
                 "Unknown error occurred during your request.")
       :class_name "color danger"}]]}))

;;
;; Preview
;;

(rf/reg-event-fx
 ::api.search-feeds
 (fn [_ [_ url]]
   {:dispatch [::api.call :search-feeds
               {:url url}
               ::api.search-feeds.ok]}))

(rf/reg-event-db
 ::api.search-feeds.ok
 (fn [db [_ feed]]
   (assoc-in db [:search-feeds] feed)))

;;
;; Subscribe
;;


(rf/reg-event-fx
 ::api.subscribe
 (fn [_ [_ feed_id title]]
   {:dispatch [::api.call :subscribe
               {:feed_id feed_id :title title}
               ::api.subscribe.ok]}))

(rf/reg-event-db
 ::api.subscribe.ok
 (fn [db [_ feed]]




   (assoc-in db [:feeds (:id feed)] feed)))

;;
;; Subscriptions
;;

(rf/reg-event-fx
 ::api.feeds
 (fn [_ [_]]
   {:dispatch [::api.call :subscriptions nil ::api.feeds.ok]}))

(def into-map (partial into {}))

(rf/reg-event-db
 ::api.feeds.ok
 (fn [db [_ feeds]]
   (let [zipped (into-map
                 (for [feed feeds]
                   [(:id feed) feed]))]
     (assoc-in db [:feeds] zipped))))

;;
;; Unsubscribe
;;

(rf/reg-event-fx
 ::api.unsubscribe
 (fn [_ [_ sub_id]]
   {:dispatch [::api.call :unsubscribe
               {:sub_id sub_id}
               ::api.unsubscribe.ok]}))

(rf/reg-event-db
 ::api.unsubscribe.ok
 (fn [db [_ _]]
   ;; todo remove from subs
   db))

;;
;; Messages
;;

(rf/reg-event-fx
 ::api.messages
 (fn [_ [_ feed_id & [from_id]]]
   {:dispatch [::api.call :messages
               {:feed_id feed_id :from_id from_id}
               ::api.messages.ok]}))

(rf/reg-event-db
 ::api.messages.ok
 (fn [db [_ {:keys [feed_id] :as resp}]]
   (assoc-in db [:entries feed_id] resp)))

;;
;; Mark (un)read
;;

(rf/reg-event-fx
 ::api.mark-read
 (fn [_ [_ entry_id is_read]]
   {:dispatch [::api.call :mark-read
               {:entry_id entry_id
                :is_read is_read}
               ::api.mark-read.ok]}))

(rf/reg-event-db
 ::api.mark-read.ok
 (fn [db [_ entry]]
   (let [{:keys [feed_id]} entry]
     (db/upsert db
                [:entries feed_id :entries]
                :id
                entry))))

;;
;; User info
;;

(rf/reg-event-fx
 ::api.user-info
 (fn [_ [_ ]]
   {:dispatch [::api.call :user-info nil ::api.user-info.ok]}))

(rf/reg-event-db
 ::api.user-info-ok
 (fn [db [_ user]]
   (assoc db :user user)))

;;
;; Update subscription
;;

(rf/reg-event-fx
 ::api.update-subscription
 (fn [_ [_ feed_id params]]
   {:dispatch [::api.call
               :update-subscription
               (assoc params :feed_id feed_id)
               ::api.update-subscription.ok]}))

(rf/reg-event-db
 ::api.update-subscription.ok
 (fn [db [_ feed]]
   (assoc-in db [:feeds (:id feed)] feed)
   #_
   (db/upsert db
              [:feeds]
              :id
              feed)))
