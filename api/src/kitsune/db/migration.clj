(ns kitsune.db.migration
  (:require [ragtime.next-jdbc :as jdbc]
            [ragtime.repl :as repl]
            [kitsune.db :refer [datasource]]))

(defn config
  []
  {:datastore  (jdbc/sql-database datasource)
   :migrations (jdbc/load-resources "migrations")})

(defn migrate
  []
  (repl/migrate (config)))

(defn rollback
  []
  (repl/rollback (config)))
