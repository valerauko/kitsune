(ns kitsune.wrappers.logging
  (:require [com.brunobonacci.mulog :as u]
            [kitsune.logging :as log])
  (:import [java.util
            UUID]))

(defmethod log/format-line ::request
  [{::keys [status method uri remote-address user-agent runtime]}]
  ;; Processed 206 POST /api/fed/v1/inbox in 3.831ms for 136.243.7.114 via http.rb/5.1.0 (Mastodon/4.0.2; +https://mastodon.social/)
  (format "Processed %d %s %s in %.3fms for %s via %s"
          status method uri runtime remote-address user-agent))

(defn wrap-logging
  [handler]
  (fn logging-wrapper
    [{:keys [request-method uri remote-addr]
      {fwd-for "x-forwarded-for"
       user-agent "user-agent"} :headers
      :as request}]
    (u/with-context {::log/context-id (str (UUID/randomUUID))}
      (let [start (System/nanoTime)
            response (handler request)]
        (log/info ::request
                  ::status (get response :status 200)
                  ::method (-> request-method (name) (.toUpperCase))
                  ::uri uri
                  ::runtime (/ (- (System/nanoTime) start) 1000000.0)
                  ::remote-address (or fwd-for remote-addr)
                  ::user-agent (or user-agent "unknown client"))
        response))))
