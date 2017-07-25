(ns project.atom
  "Atom parser."
  (:require [project.xml :as xml]))

(defn parse [node]
  (-> node :content first)
  ;; (-> node (xml/get-node :title))
  )
