(ns kitsune.async
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
            [goose.brokers.rmq.broker :as rmq]
            [goose.client]
            [goose.worker]
            [kitsune.lang :refer [qualify-sym]]))

(def rmq-opts
  (-> rmq/default-opts
      (assoc-in [:settings :host] "queue")))

(defstate producer
  :start
  (rmq/new-producer rmq-opts)
  :stop
  (rmq/close producer))

(defmacro delayed
  ([call]
   `(delayed ~call goose.client/default-opts))
  ([call opts]
   (let [[func & args] call]
     `(future
       (goose.client/perform-async
        (merge {:broker producer} ~opts)
        '~(qualify-sym func) ~@args)))))

(defn job-logger
  [invoke]
  (fn logger-middleware
    [opts {:keys [id execute-fn-sym] :as job}]
    (let [start (System/nanoTime)
          result (invoke opts job)]
      (log/info (format "Processed job %s (%s) in %.3fms"
                        id execute-fn-sym
                        (/ (- (System/nanoTime) start) 1000000.0)))
      result)))

(defstate consumer
  :start
  (rmq/new-consumer rmq-opts)
  :stop
  (rmq/close consumer))

(defstate worker
  :start
  (goose.worker/start
   (-> goose.worker/default-opts
       (assoc :broker consumer)
       (assoc :middlewares job-logger)))
  :stop
  (goose.worker/stop worker))
