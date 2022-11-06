(ns user
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [clojure.tools.namespace.repl :refer [refresh refresh-dirs set-refresh-dirs]]
            [mount.core :refer [defstate start stop]]
            [camel-snake-kebab.core :refer [->kebab-case-string]]
            [next.jdbc :refer [with-transaction]]
            [csele.keys :refer [generate-keypair]]
            [kitsune.db :refer [defquery datasource]]
            [kitsune.lang :refer [...]]
            [kitsune.uri :refer [host url]]
            [kitsune.fed.routes :as-alias fed])
  (:import [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(set-refresh-dirs "dev" "src")

(defn reload
  []
  (stop)
  (refresh :after 'mount.core/start))

(defn now
  []
  (let [utc (ZoneId/of "UTC")
        formatter (DateTimeFormatter/ofPattern "uuuuMMddHHmmss")
        timestamp (ZonedDateTime/now utc)]
    (.format formatter timestamp)))

(defn create-migration
  [name]
  (let [base (str (now) "-" (->kebab-case-string name))
        folder "resources/migrations/"
        up-name (str folder base ".up.sql")
        down-name (str folder base ".down.sql")
        command "chown 1000:1000 "]
    (spit up-name (str "-- " up-name "\n"))
    (spit down-name (str "-- " down-name "\n"))
    (.exec (Runtime/getRuntime) (str command up-name))
    (.exec (Runtime/getRuntime) (str command down-name))))

(defquery -create-account
  [name display-name public-key]
  {:insert-into [:accounts]
   :values [{:acct (str name "@" host)
             :name name
             :uri (url :kitsune.routes/profile {:name name})
             :inbox (url ::fed/account-inbox {:name name})
             :shared-inbox (url ::fed/shared-inbox)
             :public-key public-key
             :display-name display-name}]
   :returning [:id]})

(defquery -create-user
  [account-id private-key]
  {:insert-into [:users]
   :values [(... account-id private-key)]})

(defn create-account
  [{:keys [name display-name] :or {display-name name}}]
  (with-transaction [tx datasource]
    (let [{:keys [private public]} (generate-keypair)
          [{id :accounts/id}] (-create-account tx name display-name public)]
      (-create-user tx id private)
      (... id name))))
