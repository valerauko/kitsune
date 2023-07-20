(ns user
  (:require [clojure.repl :refer [doc source]]
            [clojure.tools.namespace.repl :refer [refresh set-refresh-dirs]]
            [mount.core :refer [defstate start stop]]
            [camel-snake-kebab.core :refer [->kebab-case-string]]
            [next.jdbc :as jdbc :refer [with-transaction]]
            [honey.sql :as sql]
            [csele.keys :refer [generate-keypair]]
            [kitsune.db :refer [defquery datasource]]
            [kitsune.db.account :as acc-db]
            [kitsune.db.migration :refer [migrate rollback]]
            [kitsune.lang :refer [...]]
            [kitsune.uri :refer [host url]])
  (:import [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(sql/set-dialect! :mysql)
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

(defn -create-account
  [tx name display-name public-key]
  (acc-db/upsert
   tx
   (... name public-key display-name
        :acct (str name "@" host)
        :uri (url :kitsune.routes/profile {:name name})
        :inbox (url :kitsune.fed.routes/account-inbox {:name name})
        :shared-inbox (url :kitsune.fed.routes/shared-inbox))))

(defquery -create-user
  [account-id private-key]
  {:insert-into [:users]
   :values [(... account-id private-key)]})

(defn create-account
  [{:keys [name display-name] :or {display-name name}}]
  (with-transaction [tx datasource]
    ;; think about automated key rotation (once a year? or so?)
    (let [{:keys [private public]} (generate-keypair 4096)
          [{id :accounts/id}] (-create-account tx name display-name public)]
      (-create-user tx id private)
      (... id name))))
