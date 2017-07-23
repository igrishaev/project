(defproject project "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :repositories {"my.datomic.com"
                 {:url "https://my.datomic.com/repo"
                  :creds :gpg}}

  :plugins [[migratus-lein "0.5.0"]
            [lein-environ "1.1.0"]]

  :dependencies [[org.clojure/clojure "1.9.0-alpha17"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [mount "0.1.11"]
                 [com.datomic/datomic-pro "0.9.5561.50"]
                 [environ "1.1.0"]
                 [clj-time "0.14.0"]
                 [clj-http "3.6.1"]
                 [compojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]])
