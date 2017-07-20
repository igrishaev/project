(defproject project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[migratus-lein "0.5.0"]
            [lein-environ "1.1.0"]]

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [mount "0.1.11"]
                 [environ "1.1.0"]
                 [conman "0.6.7"]
                 [migratus "0.9.8"]
                 [org.postgresql/postgresql "42.1.3"]]

  :migratus {:store :database
             :migration-dir "migrations"
             :db ~(get (System/getenv) "DATABASE_URL")})
