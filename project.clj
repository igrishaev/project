(defproject project "0.1.0-SNAPSHOT"
  :description "Feed reader"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[migratus-lein "0.5.0"]]

  :dependencies [[org.clojure/clojure "1.9.0"]

                 ;logging
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [org.clojure/tools.logging "0.4.0"]

                 ;; db
                 [org.clojure/java.jdbc "0.6.1"]
                 [org.postgresql/postgresql "42.1.3"]
                 [migratus "0.9.8"]
                 [honeysql "0.9.3"]

                 ;; http
                 [compojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-json "0.4.0"]
                 [metosin/ring-http-response "0.9.0"] ;; delete

                 ;; misc
                 [clj-time "0.14.0"]
                 [clj-http "3.6.1"]
                 [medley "1.0.0"]
                 [oauth-clj "0.1.16"]
                 [cheshire "5.6.3"]

                 ;; html
                 [selmer "1.11.7"]
                 [markdown-clj "1.0.2"]

                 ;; email
                 [com.draines/postal "2.0.2"]
                 [buddy/buddy-core "1.4.0"]

                 ;; UI
                 [org.clojure/clojurescript  "1.9.946" :scope "provided"]

                 ;; [ring-webjars "0.2.0"]
                 [re-frame "0.10.5"]
                 [day8.re-frame/http-fx "0.1.6" :exclusions [cheshire]]
                 [cljs-ajax "0.7.3" :exclusions [cheshire]]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 ;; [com.cemerick/url "0.1.1"]
                 [secretary "1.2.3"]

                 ]

  :main project.core
  :uberjar-name "project.jar"

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
