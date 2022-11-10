(ns kitsune.cache
  (:refer-clojure :exclude [get set])
  (:require [clojure.tools.logging :as log]
            [taoensso.carmine :as car]))

(def cache-opts
  {:pool {}
   :spec {:host "cache"
          :port 6379}})

(defmacro ops
  [& body]
  `(car/wcar
    cache-opts
    :as-pipeline
    ~@body))

(defn get
  [k]
  (try
    (->> k
         (car/get)
         (ops cache-opts :as-pipeline)
         (first))
    (catch Throwable ex
      (log/warn ex)
      nil)))

(defn set
  [k v]
  (future
   (try
     (ops (car/set k v))
     (catch Throwable ex
       (log/warn ex)
       nil)))
  v)
