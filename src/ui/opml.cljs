(ns io.opml
  (:require
   ;; todo deps
   [clojure.string :as str]

   [ajax.core :as ajax]

            [reagent.core :as r]
            [re-frame.core :as rf])

  )


(defn import-button
  []
  [:div
   [:input
    {:type :file
     :on-change
     (fn [e]
       (prn (.. e -target -files))
       )

     }


    ]]
  )

(rf/reg-event-fx
 :api/import-opml
 (fn [_ [_ payload]]
   {:http-xhrio {:method :post
                 :uri "/api"
                 :body payload
                 :headers {"Content-Type" "text/xml"}
                 :response-format
                 (ajax/json-response-format
                  {:keywords? true})
                 :on-success [:api/import-opml.ok]
                 :on-failure [:api/import-opml.err]}}))


(rf/reg-event-fx
 :api/import-opml.ok
 (fn [_ [_ ]]
   {:dispatch [:bar/info "ok"]}))


(rf/reg-event-fx
 :api/import-opml.err
 (fn [_ [_ ]]
   {:dispatch [:bar/error "error"]}))
