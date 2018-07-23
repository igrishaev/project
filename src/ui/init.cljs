(ns ui.init
  (:require [re-frame.core :as rf]))


(rf/reg-event-fx
 :init/init
 (fn [db [_ user]]
   {:dispatch [:ui.events/api.call
               :user-info nil
               [:init/init.ok]]}))


(rf/reg-event-fx
 :init/init.ok
 (fn [db [_ user]]
   {:dispatch-n [[:ui.events/api.user-info.ok user]
                 [:ui.events/api.feeds]]}))


(defn init
  []
  (rf/dispatch [:init/init]))
