(ns project.sources
  (:require [project.db :as db]
            [project.http :as http]
            [project.feed :refer [parse-payload]]
            [clj-time.core :as t]
            [environ.core :refer [env]]))


(defn update-source [source]
  (let [url      (:url_src source)
        response (http/get url)
        payload  (:body response)
        ctype    (-> response :headers (get "Content-Type"))
        feed     (parse-payload ctype payload)]
    feed

    ;; (db/with-trx
    ;;   (db/update! :sources
    ;;               {:title title
    ;;                :description description
    ;;                :last_update_ok true
    ;;                :last_update_msg ""
    ;;                ;; :update_count
    ;;                ;; :message_count

    ;;                }


    ;;               ["id = ?" (:id source)])
    ;;   )

    )

)

;; (defn get-next-sync-date []
;;   (t/plus (t/now) (t/seconds (:source-next-sync-in env 600))))

;; todo transcations

;; (defn sync-sources []
;;   (let [sources (db/get-sources-to-sync {:limit 42})]
;;     (when-not (empty? sources)
;;       (let [next_sync (get-next-sync-date)]
;;         (db/mark-sources-synced {:ids (map :id sources)
;;                                  :next_sync next_sync}))

;;       (doseq [source sources]
;;         (update-source source)))))
