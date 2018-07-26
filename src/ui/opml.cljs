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
       (let [input (.. e -target)
             file (-> e .-target .-files (.item 0))
             reader (js/FileReader.)]

         (set! (.-onload reader)
               (fn [e]
                 (let [content (.. e -target -result)]
                   (rf/dispatch [:api/import-opml input content]))))

         (set! (.-onerror reader)
               (fn [e]
                 (rf/dispatch [:bar/error "Cannot read the file you've chosen."])))

         (.readAsText reader file)))}]])


(rf/reg-event-fx
 :api/import-opml
 (fn [_ [_ input opml]]
   {:dispatch [:ui.events/api.call :import-opml
               {:opml opml}
               [:api/import-opml.ok input]]}))


(rf/reg-event-fx
 :api/import-opml.ok
 (fn [_ [_ input data]]
   (set! (.. input -value) "")
   {:dispatch [:bar/info "ok"]}))
