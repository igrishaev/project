(ns project.spec.email
  (:require [project.spec.util :refer [->int]]

            [clojure.spec.alpha :as s]
            [clojure.string :as str]))

(def re-email? (partial re-matches #".+?@.+?\..+?"))

(s/def ::email
  (s/and
   string?
   (s/conformer str/trim)
   (s/conformer str/lower-case)
   not-empty
   re-email?))

(s/def ::expires ->int)
(s/def ::signature string?)

(s/def ::auth-init
  (s/keys :req-un [::email]))

(s/def ::auth-back
  (s/keys :req-un [::email
                   ::expires
                   ::signature]))
