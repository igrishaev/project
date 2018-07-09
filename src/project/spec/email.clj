(ns project.spec.email
  (:require [project.spec.util
             :refer [->int ->email]]

            [clojure.spec.alpha :as s]))

(s/def ::email ->email)
(s/def ::expires ->int)
(s/def ::signature string?)

(s/def ::auth-init
  (s/keys :req-un [::email]))

(s/def ::auth-back
  (s/keys :req-un [::email
                   ::expires
                   ::signature]))
