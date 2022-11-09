(ns kitsune.core
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate start stop]]
            [kitsune.db]
            [kitsune.db.migration :refer [migrate rollback]]
            [org.httpkit.server :as http]
            [kitsune.routes :as routes]))

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

(defstate http-server
  :start
  (let [server (http/run-server
                (wrap-async routes/handler)
                {:port 3000
                 :threads (* 2 (.availableProcessors (Runtime/getRuntime)))
                 :queue-size 50
                 :max-body 44739243})]
    (log/info "API server running at :3000")
    server)
  :stop
  (do
    (log/info "Stopping server...")
    (http-server :timeout 1000)))

(defn shutdown
  [& _]
  (log/info "Shutting down...")
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
