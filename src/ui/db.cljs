(ns ui.db
  (:require [com.rpl.specter :as s]))

(defn upsert [db path key-eq new]
  (letfn [(map-eq? [old] (= (get new key-eq) (get old key-eq)))
          (updater [old] [(merge old new)])]
    (let [full-path (conj path s/ALL map-eq?)
          [db2 updated?] (s/replace-in full-path updater db)]
      (if updated?
        db2
        (s/setval (conj (vec path) s/AFTER-ELEM) new db)))))
