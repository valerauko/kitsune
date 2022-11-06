(ns kitsune.db
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
            [hikari-cp.core :refer [make-datasource close-datasource]]
            [honey.sql :as sql]
            [next.jdbc]
            [next.jdbc.result-set :as rs])
  (:import [org.mariadb.jdbc
            MariaDbBlob]))

(defstate datasource
  :start
  (make-datasource {:jdbc-url "jdbc:mariadb://db:3306/kitsune"
                    :username "kitsune"
                    :password "whatever"})
  :stop
  (close-datasource datasource))

(defn process-query
  [])

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
            (try
              (let [start# (System/nanoTime)
                    sql-dsl# (do ~@body)
                    text# (sql/format sql-dsl#)
                    result# (next.jdbc/execute! datasource# text#)]
                (log/debug (format "%.3fms" (/ (- (System/nanoTime) start#) 1000000.0))
                           (pr-str text#))
                (if (= 1 (:limit sql-dsl#))
                  (first result#)
                  result#)))))))))
