(defproject project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[migratus-lein "0.5.0"]
            [lein-environ "1.1.0"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [mount "0.1.11"]
                 [environ "1.1.0"]
                 [conman "0.6.7" :exclusions [org.slf4j/slf4j-api]]
                 [migratus "0.9.8"]
                 [clj-time "0.14.0"]
                 [clj-http "3.6.1"]
                 [org.postgresql/postgresql "42.1.3"]]

  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")})
