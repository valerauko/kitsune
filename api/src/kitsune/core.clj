(ns kitsune.core
  (:require [mount.core :refer [defstate start stop]]
            [kitsune.db]
            [kitsune.db.migration :refer [migrate rollback]]
            [kitsune.lang :refer [...]]
            [kitsune.logging :as log]
            [kitsune.routes :as routes]
            [org.httpkit.server :as http]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

(defn wrap-async
  [ring-handler]
  (fn -async-wrap [request]
    (when-let [channel (:async-channel request)]
      (try
        (let [response (ring-handler request)]
          (http/send! channel response))
        (catch Throwable error
          (log/error "Error while processing request" error)
          (http/send! channel {:status 500})
          (http/close channel)))
      {:body channel})))

(derive ::started ::server-event)
(derive ::stopping ::server-event)

(defmethod log/format-line ::server-event
  [{event :mulog/event-name}]
  (->> event (keyword) (name) (format "Server %s...")))

(defstate http-server
  :start
  (let [port 3000
        threads (* 2 (.availableProcessors (Runtime/getRuntime)))
        queue-size 50
        max-body 44739243
        server (http/run-server
                (wrap-async routes/handler)
                (... port threads queue-size max-body))]
    (log/info ::started
              ::port port
              ::threads threads
              ::queue queue-size
              ::max-body max-body)
    server)
  :stop
  (do
    (log/info ::stopping)
    (http-server :timeout 1000)))

(defn shutdown
  [& _]
  (stop))

(defn -main
  [& args]
  (cond
    (some #{"migrate"} args)
    (do
      (start #'kitsune.db/datasource)
      (migrate)
      (System/exit 0))

    (some #{"rollback"} args)
    (do
      (start #'kitsune.db/datasource)
      (rollback)
      (System/exit 0))

    :else
    (do
      (start)
      (.addShutdownHook
       (Runtime/getRuntime)
       (Thread. ^Runnable shutdown)))))
