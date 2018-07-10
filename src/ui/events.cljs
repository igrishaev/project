(ns ui.events
  (:require

            ;; [ui.url :as url]
            ;; [ui.util :refer [format to-text]]

   [day8.re-frame.http-fx]
   [re-frame.core :as rf]
   [ajax.core :as ajax]))

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
 ::api.preview
 (fn [_ [_ url]]
   {:dispatch [::api.call :preview
               {:url url}
               ::api.preview.ok]}))

(rf/reg-event-db
 ::api.preview.ok
 (fn [db [_ feed]]
   (update-in db [:preview] feed)))

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
 (fn [db [_ sub]] ;; todo put in a proper place
   (update-in db [:dashboard :subs] conj sub)))

;;
;; Subscriptions
;;

(rf/reg-event-fx
 ::api.subs
 (fn [_ [_]]
   {:dispatch [::api.call :subscriptions nil ::api.subs.ok]}))

(rf/reg-event-db
 ::api.subs.ok
 (fn [db [_ subs]]
   (assoc-in db [:dashboard :subs] subs)))

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
 (fn [_ [_ sub_id & [from_id]]]
   {:dispatch [::api.call :messages
               {:sub_id sub_id :from_id from_id}
               ::api.messages.ok]}))

(rf/reg-event-db
 ::api.messages.ok
 (fn [db [_ {:keys [sub_id] :as resp}]]
   (assoc-in db [:messages sub_id] resp)))

;;
;; Mark (un)read
;;

(rf/reg-event-fx
 ::api.mark-read
 (fn [_ [_ sub_id message_id is_read]]
   {:dispatch [::api.call :mark-read
               {:sub_id sub_id
                :message_id message_id
                :is_read is_read}
               ::api.mark-read.ok]}))

(rf/reg-event-db
 ::api.mark-read.ok
 (fn [db [_ ]]
   ;; todo mark read in the db
   db))
