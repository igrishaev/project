(ns ui.subs
  (:require [re-frame.core :as rf]))

(re-frame.core/reg-sub
 ::page
 (fn [db _]
   (:page db)))

(rf/reg-sub
 ::feeds
 (fn [db [_]]
   (-> db :feeds vals))) ;; todo sorting?

(rf/reg-sub
 ::loader
 (fn [db [_]]
   (:loader db)))

(rf/reg-sub
 ::entries
 (fn [db [_ feed_id]]
   (vec
    (map-indexed
     (fn [index entry]
       [index (:id entry)])
     (get-in db [:entries feed_id])))))

(rf/reg-sub
 ::entry-count
 (fn [db [_ feed_id]]
   (-> db :entries (get feed_id) count)))

(rf/reg-sub
 ::find-entry
 (fn [db [_ feed_id index]]
   (get-in db [:entries feed_id index])))

(rf/reg-sub
 ::find-feed
 (fn [db [_ feed_id]]
   (get-in db [:feeds feed_id])))

(rf/reg-sub
 ::search-feeds
 (fn [db [_ ]]
   (get db :search-feeds)))
