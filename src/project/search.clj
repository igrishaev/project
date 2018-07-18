(ns project.search
  (:require [project.http :as http]

            [hickory.core :as hickory]
            [hickory.select :as select]
            )

  )



(def types
  #{"application/rss+xml"
    "application/atom+xml"})

(def feed-selector

  (select/and

   (select/tag :link)

   (select/attr
    :rel
    (fn [rel]
      (= rel "alternate")))

   (select/attr
    :type
    (fn [type]
      (get types type)))))

(defn find-feeds
  [url]

  (let [response (http/get url)
        {body :body} response]

    (->> body
         hickory.core/parse
         hickory.core/as-hickory
         (select/select feed-selector))))
