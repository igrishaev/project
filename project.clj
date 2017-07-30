(defproject project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[migratus-lein "0.5.0"]]

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [mount "0.1.11"]
                 [conman "0.6.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.postgresql/postgresql "42.1.3"]
                 [migratus "0.9.8"]
                 [cprop "0.1.10"]
                 [clj-time "0.14.0"]
                 [clj-http "3.6.1"]
                 [compojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]]

  :resource-paths ["resources"]

  :profiles {:dev {:resource-paths ["env/dev/resources"]}
             :test {:resource-paths ["env/dev/resources"
                                     "env/test/resources"]}}
  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")})
