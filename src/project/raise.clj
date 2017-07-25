(ns project.raise
  (:require [clojure.string :as str]))

(defn raise [& args]
  (throw (Exception. (str/join \space args))))
