(ns kitsune.db
  (:require [com.brunobonacci.mulog :as u]
            [kitsune.logging :as log]
            [mount.core :refer [defstate]]
            [hikari-cp.core :refer [make-datasource close-datasource]]
            [honey.sql :as sql]
            [next.jdbc]
            [next.jdbc.date-time]
            [next.jdbc.result-set :as rs]
            [kitsune.lang :refer [qualify-sym]])
  (:import [clojure.lang
            ExceptionInfo]
           [org.mariadb.jdbc
            MariaDbBlob]))

(defstate datasource
  :start
  ;; need to use jdbc-url because the hikari-cp hardcoded
  ;; mariadb adapter class isn't right for this version
  (make-datasource {:jdbc-url "jdbc:mariadb://mariadb:3306/kitsune"
                    :username "kitsune"
                    :password "whatever"})
  :stop
  (close-datasource datasource))

(defmethod log/format-line ::query
  [{::keys [name query runtime]}]
  ;; 2.685ms kitsune.db.note/find-by-uri ["SELECT * FROM `notes` WHERE `uri` = ? LIMIT ?" "https://mk.absturztau.be/notes/8s0cktq1fg" 1]
  (format "%.3fms %s %s" runtime name query))

(defn run-query!
  [query-name query-fn datasource]
  (try
    (let [start (System/nanoTime)
          sql-dsl (query-fn)
          text (sql/format
                sql-dsl
                {:dialect :mysql
                 :quoted-snake :true})
          result (next.jdbc/execute!
                  datasource
                  text
                  {:builder-fn rs/as-kebab-maps})]
      (log/debug ::query
                 ::runtime (/ (- (System/nanoTime) start) 1000000.0)
                 ::name query-name
                 ::query (pr-str text))
      (if (= 1 (:limit sql-dsl))
        (first result)
        result))
    (catch ExceptionInfo ex
      (log/error ::error (ex-data ex) (.getMessage ex))
      (throw ex))))

(defmacro defquery
  [name & decl]
  (loop [meta []
         stage ::meta
         items decl]
    (case stage
      ::meta
      (let [head (first items)]
        (cond
          ;; docstring
          (string? head) (recur [head] ::meta (rest items))
          ;; attr-map
          (map? head) (recur (conj meta head) ::main (rest items))
          :else (recur meta ::main items)))

      ::main
      (let [args (first items)
            body (rest items)]
        `(defn ~name
           ~@meta
           ~(let [dummy-args# (mapv gensym args)]
              `(~dummy-args#
                (~name kitsune.db/datasource ~@dummy-args#)))
           ([datasource# ~@args]
            (let [query-fn# (fn [] ~@body)]
              (run-query! ~(str (qualify-sym name)) query-fn# datasource#))))))))
