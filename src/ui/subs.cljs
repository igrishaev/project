(ns ui.subs
  (:require [re-frame.core :as rf]))

(re-frame.core/reg-sub
 ::page
 (fn [db _]
   (:page db)))

(rf/reg-sub
 ::subs
 (fn [db [_]]
   (get-in db [:dashboard :subs])))

(rf/reg-sub
 ::messages
 (fn [db [_ sub_id]]
   (get-in db [:messages sub_id :messages])))

;; todo might be slow

(rf/reg-sub
 ::find-sub
 (fn [db [_ sub_id]]
   (first
    (filter
     (fn [item]
       (-> item :sub :id (= sub_id)))
     (get-in db [:dashboard :subs])))))
