(ns project.test.fetch
  (:require [clojure.test :refer :all]
            [project.feed :refer [fetch-feed]]
            [project.test.fixtures :refer [server-fixture]]))


(use-fixtures
  :once
  server-fixture)

(deftest fetch-test
  (let [feed (fetch-feed "http://127.0.0.1:4000/lenta.ru.xml")]
    (is feed)))
