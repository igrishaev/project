(ns ui.events
  (:require [day8.re-frame.http-fx]
            [re-frame.core :as rf]
            [ajax.core :as ajax]))

;; todo remove that
(defn vec->map
  [items]
  (into {} (for [item items]
             [(:id item) item])))

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
                 :response-format (ajax/json-response-format
                                   {:keywords? true})
                 :on-success event-ok
                 :on-failure (or event-err [::api.error])}}))

(rf/reg-event-fx
 ::api.error
 (fn [_ [& args]]
   (js/console.log args #_(clj->js data))
   ;; todo show bar
   nil
   #_
   {:dispatch-n
    [[::notification
      {:title "Error"
       :text (or (some-> data :response :error)
                 "Unknown error occurred during your request.")
       :class_name "color danger"}]]}))

;;
;; Loader
;;

(rf/reg-event-db
 ::loader
 (fn [db [_ data]]
   (assoc db :loader data)))

;;
;; Preview
;;

(rf/reg-event-fx
 ::api.search-feeds
 (fn [_ [_ term]]
   {:dispatch [::api.call :search-feeds
               {:term term}
               [::api.search-feeds.ok]]}))

(rf/reg-event-db
 ::api.search-feeds.ok
 (fn [db [_ feeds]]
   (assoc-in db [:search-feeds] feeds)))

;;
;; Subscribe
;;


(rf/reg-event-fx
 ::api.subscribe
 (fn [_ [_ feed_id title]]
   {:dispatch [::api.call :subscribe
               {:feed_id feed_id :title title}
               [::api.subscribe.ok]]}))


(rf/reg-event-fx
 ::api.subscribe.ok
 (fn [{db :db} [_ feed]]
   (let [{feed-id :id} feed]
     {:db (assoc-in db [:feeds feed-id] feed)
      :dispatch [:nav/goto-feed feed-id]})))

;;
;; Subscriptions
;;

(rf/reg-event-fx
 ::api.feeds
 (fn [_ [_]]
   {:dispatch [::api.call
               :subscriptions nil
               [::api.feeds.ok]]}))


(rf/reg-event-db
 ::api.feeds.ok
 (fn [db [_ feeds]]
   (assoc db :feeds (vec->map feeds))))

;;
;; Unsubscribe
;;

(rf/reg-event-fx
 ::api.unsubscribe
 (fn [_ [_ feed_id]]
   {:dispatch [::api.call :unsubscribe
               {:feed_id feed_id}
               [::api.unsubscribe.ok]]}))

;; todo redirect somewhere!

(rf/reg-event-db
 ::api.unsubscribe.ok
 (fn [db [_ {feed_id :feed_id}]]
   (update db :feeds dissoc feed_id)))

;;
;; Messages
;;

(rf/reg-event-fx
 ::api.messages
 (fn [_ [_ feed_id]]
   {:dispatch [::api.call :messages
               {:feed_id feed_id}
               [::api.messages.ok feed_id]]}))

(rf/reg-event-db
 ::api.messages.ok
 (fn [db [_ feed_id entries]]
   (assoc-in db [:entries feed_id] entries)))

;;
;; Read more
;;

;; todo separated event

(rf/reg-event-fx
 ::api.read-more
 (fn [_ [_ feed_id last_id]]
   {:dispatch-n
    [[::api.call :messages
      {:feed_id feed_id :last_id last_id}
      [::api.read-more.ok feed_id]]
     [::loader true]]}))

;; todo separated event

(rf/reg-event-fx
 ::api.read-more.ok
 (fn [{db :db} [_ feed_id entries]]
   (let [is-empty (empty? entries)]
     {:db (if-not is-empty
            (update-in db [:entries feed_id] into entries)
            db)
      :dispatch [::loader false]})))

;;
;; Mark (un)read
;;

(rf/reg-event-fx
 ::api.mark-read
 (fn [_ [_ index entry_id is_read]]
   {:dispatch [::api.call :mark-read
               {:entry_id entry_id
                :is_read is_read}
               [::api.mark-read.ok index]]}))

(rf/reg-event-db
 ::api.mark-read.ok
 (fn [db [_ index entry]]
   (let [{:keys [feed_id]} entry]
     (assoc-in db [:entries feed_id index] entry))))

;;
;; User info
;;

(rf/reg-event-fx
 ::api.user-info
 (fn [_ [_ ]]
   {:dispatch [::api.call
               :user-info nil
               [::api.user-info.ok]]}))

(rf/reg-event-db
 ::api.user-info.ok
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
               [::api.update-subscription.ok feed_id]]}))

;; todo separate

(rf/reg-event-fx
 ::api.update-subscription.ok
 (fn [{db :db} [_ feed_id feed]]
   {:db (assoc-in db [:feeds feed_id] feed)
    :dispatch [::api.messages feed_id]}))

;; todo rename

(rf/reg-event-db
 ::feed-read-count-inc
 (fn [db [_ feed_id]]
   (update-in db [:feeds feed_id :sub :message_count_unread] inc)))

(rf/reg-event-db
 ::feed-read-count-dec
 (fn [db [_ feed_id]]
   (update-in db [:feeds feed_id :sub :message_count_unread] dec)))
