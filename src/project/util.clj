(ns project.util)

(defn uuid []
  (str (java.util.UUID/randomUUID)))
