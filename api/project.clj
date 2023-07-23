(defproject social.kitsune/kitsune "0.3.0"
  :description "Very fox microblogging service"
  :url "https://kitsune.social"
  :license {:name "AGPL-3.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.gnu.org/licenses/agpl.txt"}
  :main ^:skip-aot kitsune.core
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.clojure/data.xml "0.2.0-alpha7"]
                 [ch.qos.logback/logback-classic "1.4.8"]
                 [com.brunobonacci/mulog "0.9.0"]
                 [mount "0.1.17"]
                 [http-kit "2.7.0"] ;; update fed/http UA if changed
                 [metosin/jsonista "0.3.7"]
                 [metosin/malli "0.11.0"]
                 [metosin/muuntaja "0.6.8"
                  :exclusions [metosin/jsonista]]
                 [metosin/reitit "0.6.0"
                  :exclusions [metosin/jsonista
                               org.clojure/core.rrb-vector]]
                 [metosin/ring-http-response "0.9.3"
                  :exclusions [ring/ring-core]]
                 [com.taoensso/carmine "3.2.0"
                  :exclusions [com.taoensso/encore]]
                 [com.taoensso/encore "3.62.1"]
                 [com.nilenso/goose "0.3.2"]
                 [buddy/buddy-auth "3.0.323"]
                 [buddy/buddy-hashers "2.0.167"]
                 [buddy/buddy-sign "3.5.351"]
                 [org.mariadb.jdbc/mariadb-java-client "3.1.4"]
                 [hikari-cp "3.0.1"]
                 [com.github.seancorfield/next.jdbc "1.3.883"]
                 [dev.weavejester/ragtime.core "0.9.3"]
                 [dev.weavejester/ragtime.next-jdbc "0.9.3"]
                 [com.github.seancorfield/honeysql "2.4.1045"]
                 [social.kitsune/csele "0.7.0"]]
  :profiles {:dev {:source-paths ["dev"]
                   :repl-options {:init-ns user}
                   :dependencies [[org.clojure/tools.namespace "1.4.4"]]
                   :plugins [[lein-ancient "0.7.0"]]}})
