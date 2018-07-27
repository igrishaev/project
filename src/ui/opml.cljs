(ns io.opml
  (:require [clojure.string :as str]

            [reagent.core :as r]
            [re-frame.core :as rf]))


(defn import-button
  []
  [:div

   [:label#import-opml-btn
    {:for "import-opml"}

    [:div "Import OPML"]]

   [:input
    {:id "import-opml"
     :style {:display :none}
     :type :file
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


(def ok-note
  "Your feeds are loaded. Now give us some time to update them.")


(def refresh-every (* 1000 3))

(def refresh-duration (* 1000 60 2))


(defn start-auto-refresh
  []
  (let [timer
        (js/setInterval
         #(rf/dispatch [:ui.events/api.feeds])
         refresh-every)]

    (js/setTimeout
     #(js/clearInterval timer)
     refresh-duration)))


(rf/reg-event-fx
 :api/import-opml.ok
 (fn [_ [_ input data]]
   (set! (.. input -value) "")
   (start-auto-refresh)
   {:dispatch-n [[:ui.events/api.feeds]
                 [:bar/info ok-note]]}))
