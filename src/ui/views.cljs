(ns ui.views
  (:require [ui.events]

            [re-frame.core :as rf]

            )

  )

(defn foobar
  []
  [:button
   {:on-click
    #(rf/dispatch [:ui.events/api.subs])
    }
   "test"
   ]

  )
