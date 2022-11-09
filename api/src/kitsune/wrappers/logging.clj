(ns kitsune.wrappers.logging
  (:require [clojure.tools.logging :as log]))

(defn wrap-logging
  [handler]
  (fn logging-wrapper
    [{:keys [request-method uri remote-addr]
      {fwd-for :X-Forwarded-For} :headers
      :as request}]
    (let [start (System/nanoTime)
          response (handler request)]
      (log/info (format "Processed %d %s %s in %.3fms for %s"
                        (get response :status 200)
                        (-> request-method (name) (.toUpperCase))
                        uri
                        (/ (- (System/nanoTime) start) 1000000.0)
                        (or fwd-for remote-addr)))
      response)))
