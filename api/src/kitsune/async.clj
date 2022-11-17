(ns kitsune.async
  (:require [kitsune.logging :as log]
            [com.brunobonacci.mulog :as u]
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

(defmethod log/format-line ::job
  [{::keys [job] runtime :mulog/duration}]
  ;; Processed job kitsune.fed.inbox/handle-activity in 4.362ms
  (format "Processed job %s in %.3fms" job (/ runtime 1000000.0)))

(defn job-logger
  [invoke]
  (fn logger-middleware
    [opts {:keys [id execute-fn-sym] :as job}]
    (u/with-context {::log/context-id id}
      (u/trace ::job
       {:pairs [::job execute-fn-sym]}
       (invoke opts job)))))

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
