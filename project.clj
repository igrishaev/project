(defproject project "0.1.0-SNAPSHOT"
  :description "Feed reader"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[migratus-lein "0.5.0"]]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]

                 [org.clojure/tools.logging "0.4.0"]

                 [org.postgresql/postgresql "42.1.3"]
                 [migratus "0.9.8"]

                 [clj-time "0.14.0"]
                 [clj-http "3.6.1"]
                 [compojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]
                 [honeysql "0.9.3"]
                 [medley "1.0.0"]

                 ]

  :resource-paths ["resources"]

  :profiles {:dev {:resource-paths ["env/dev/resources"]}
             :test {:resource-paths ["env/dev/resources"
                                     "env/test/resources"]}}
  :migratus {:store :database
             :db ~(let [env (-> "config.edn" slurp read-string)]
                    {:dbtype   "postgresql"
                     :host     (:db-host env)
                     :port     (:db-port env)
                     :dbname   (:db-database env)
                     :user     (:db-user env)
                     :password (:db-password env)})}
)
