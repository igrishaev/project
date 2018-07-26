(ns io.opml
  (:require [clojure.string :as str]

            [reagent.core :as r]
            [re-frame.core :as rf]))


(defn import-button
  []
  [:div
   [:input
    {:type :file
     :on-change
     (fn [e]
       (let [limit (* 1024 1024)

             input (.. e -target)
             file (-> e .-target .-files (.item 0))
             filesize (.-size file)
             filetype (.-type file)

             reader (js/FileReader.)]

         (if (< filesize limit)
           (do
             (set! (.-onload reader)
                   (fn [e]
                     (let [content (.. e -target -result)]
                       (rf/dispatch [:api/import-opml input content]))))
             (set! (.-onerror reader)
                   (fn [e]
                     (rf/dispatch [:bar/error "Cannot read the file you've chosen."])))
             (.readAsText reader file))

           (do
             (rf/dispatch [:bar/error "The file is too large."])
             (set! (.. input -value) "")))))}]])


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
   {:dispatch [:bar/info
               "Your feeds are loaded. Now give us some time to process them. Press the 'Refresh' button in a moment."]}))
