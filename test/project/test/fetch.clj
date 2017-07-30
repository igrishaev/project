(ns project.test.fetch
  (:require [clojure.test :refer :all]
            [project.feed :refer [fetch-feed]]
            [project.test.fixtures :refer [server-fixture]]))


(use-fixtures
  :once
  server-fixture)

(deftest fetch-xml-rss
  (let [feed (fetch-feed "http://127.0.0.1:4000/lenta.ru.rss.xml")]
    (is feed)))

(deftest fetch-xml-atom
  (let [feed (fetch-feed "http://127.0.0.1:4000/blog.case.edu.atom.xml")]
    (-> feed (dissoc :items) (= 1) is)
    (is feed)))
