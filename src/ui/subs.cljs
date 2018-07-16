(ns ui.subs
  (:require [re-frame.core :as rf]

            [ui.db :as db]

            ))

(re-frame.core/reg-sub
 ::page
 (fn [db _]
   (:page db)))

(rf/reg-sub
 ::feeds
 (fn [db [_]]
   (-> db :feeds vals))) ;; todo sorting?

(rf/reg-sub
 ::entries
 (fn [db [_ feed_id]]
   (-> db :entries (get feed_id) vals))) ;; todo sorting?

(rf/reg-sub
 ::entry-ids
 (fn [db [_ feed_id]]
   (-> db :entries (get feed_id) keys)))

(rf/reg-sub
 ::find-entry
 (fn [db [_ feed_id entry_id]]
   (get-in db [:entries feed_id entry_id])))

(rf/reg-sub
 ::find-feed
 (fn [db [_ feed_id]]
   (get-in db [:feeds feed_id])))

(rf/reg-sub
 ::search-feeds
 (fn [db [_ ]]
   (get db :search-feeds)))
