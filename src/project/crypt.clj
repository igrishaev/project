(ns project.crypt
  (:require [buddy.core.mac :as mac]
            [buddy.core.codecs :as codecs]
            [project.env :refer [env]]))

(def crypt-key (:crypt-key env))

(defn hmac
  [msg]
  (-> (mac/hash msg {:key crypt-key) :alg :hmac+sha256})
      (codecs/bytes->hex)))

(defn sign-map
  [data]
  (let [signature (-> data hash str hmac)]
    (assoc data :signature signature)))

(defn verify-signed-map
  [data]
  (let [sig (:signature data)
        to-sign (dissoc data :signature)
        msg (sign-map to-sign)]
    (= sig (-> to-sign sign-map :signature))))
