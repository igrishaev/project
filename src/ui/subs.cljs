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
