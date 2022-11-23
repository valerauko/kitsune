(ns kitsune.wrappers.logging
  (:require [com.brunobonacci.mulog :as u]
            [kitsune.logging :as log])
  (:import [java.util
            UUID]))

(defmethod log/format-line ::request
  [{::keys [status method uri remote-address user-agent] runtime :mulog/duration}]
  ;; Processed 206 POST /api/fed/v1/inbox in 3.831ms for 136.243.7.114 via http.rb/5.1.0 (Mastodon/4.0.2; +https://mastodon.social/)
  (format "Processed %d %s %s in %.3fms for %s via %s"
          (or status 500) method uri (/ runtime 1000000.0) remote-address user-agent))

(defn wrap-logging
  [handler]
  (fn logging-wrapper
    [{:keys [request-method uri remote-addr]
      {fwd-for "x-forwarded-for"
       user-agent "user-agent"} :headers
      :as request}]
    (u/with-context {::log/context-id (str (UUID/randomUUID))}
      (u/trace ::request
       {:pairs
        [::log/level :info
         ::method (-> request-method (name) (.toUpperCase))
         ::uri uri
         ::remote-address (or fwd-for remote-addr)
         ::user-agent (or user-agent "unknown client")]
        :capture (fn log-capture [res] {::status (get res :status 200)})}
       (handler request)))))
