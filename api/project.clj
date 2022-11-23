(defproject social.kitsune/kitsune "0.3.0"
  :description "Very fox microblogging service"
  :url "https://kitsune.social"
  :license {:name "AGPL-3.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.gnu.org/licenses/agpl.txt"}
  :main ^:skip-aot kitsune.core
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.clojure/data.xml "0.2.0-alpha7"]
                 [ch.qos.logback/logback-classic "1.4.4"]
                 [com.brunobonacci/mulog "0.9.0"]
                 [mount "0.1.16"]
                 [http-kit "2.6.0"] ;; update fed/http UA if changed
                 [metosin/jsonista "0.3.6"]
                 [metosin/malli "0.9.2"]
                 [metosin/muuntaja "0.6.8"
                  :exclusions [metosin/jsonista]]
                 [metosin/reitit "0.5.18"
                  :exclusions [metosin/jsonista
                               org.clojure/core.rrb-vector]]
                 [metosin/ring-http-response "0.9.3"
                  :exclusions [ring/ring-core]]
                 [com.taoensso/carmine "3.1.0"
                  :exclusions [com.taoensso/encore]]
                 [com.taoensso/encore "3.32.0"]
                 [com.nilenso/goose "0.3.0"]
                 [buddy/buddy-hashers "1.8.158"]
                 [buddy/buddy-sign "3.4.333"]
                 [org.mariadb.jdbc/mariadb-java-client "3.0.8"]
                 [hikari-cp "3.0.0"]
                 [com.github.seancorfield/next.jdbc "1.3.847"]
                 [dev.weavejester/ragtime.core "0.9.2"]
                 [dev.weavejester/ragtime.next-jdbc "0.9.2"]
                 [com.github.seancorfield/honeysql "2.3.928"]
                 [social.kitsune/csele "0.7.0"]]
  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[org.clojure/tools.namespace "1.3.0"]]
                   :plugins [[lein-ancient "0.7.0"]]}})
