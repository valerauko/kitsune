(ns kitsune.fed.http
  (:require [clojure.tools.logging :as log]
            [org.httpkit.client :as http]
            [org.httpkit.sni-client :as sni-client]
            [jsonista.core :as json]
            [csele.headers :refer [sign-request]]
            [csele.hash :refer [hash-base64]])
  (:import [java.net URI]
           [java.time ZonedDateTime ZoneId]
           [java.time.format DateTimeFormatter]))

(alter-var-root #'org.httpkit.client/*default-client*
                (fn force-sni [_] sni-client/default-client))

(def default-context
  {(keyword "@context") ["https://www.w3.org/ns/activitystreams"]})

(def default-content-type
  (str "application/ld+json;"
       "profile=\"https://www.w3.org/ns/activitystreams\";"
       "charset=utf-8"))

(def default-user-agent
  "http-kit/2.6.0 (Kitsune/0.3.0)")

(defn fetch-resource
  "Only used for public resources (profiles etc)"
  [uri]
  (let [start (System/nanoTime)]
    (http/get
     uri
     ;; TODO: timeouts
     {:headers {"accept" default-content-type
                "user-agent" default-user-agent}
      :timeout 5000}
     (fn fetch-resource-callback
       [{:keys [error status] :as response}]
       (let [log-msg (format "Resource GET from %s completed %s in %.3fms"
                             uri (or status "with network error")
                             (/ (- (System/nanoTime) start) 1000000.0))]
         (if error
           (log/warn log-msg error)
           (log/info log-msg))
         response)))))

(def default-signed-headers
  ["(request-target)" "host" "date" "digest" "content-type"])

(defn header-time
  []
  (let [gmt (ZoneId/of "GMT")
        ;; for some reason RFC_1123_DATE_TIME isn't good enough. not sure where
        ;; the problem is but mastodon fails to validate the date if the day
        ;; number doesn't have a leading zero (and it doesn't in the rfc format)
        formatter (DateTimeFormatter/ofPattern "EEE, dd MMM uuu HH:mm:ss zzz")
        timestamp (ZonedDateTime/now gmt)]
    (.format formatter timestamp)))

(defn send-activity
  [{:keys [inbox key-map activity headers content-type]
    :or {headers default-signed-headers
         content-type default-content-type}
    :as options}]
  (let [start (System/nanoTime)
        edn-body (->> activity (merge default-context))
        body (->> activity (merge default-context) json/write-value-as-bytes)
        target-uri (URI. inbox)
        request-headers {"host" (.getHost target-uri)
                         "date" (header-time)
                         "digest" (str "SHA-256=" (hash-base64 body))
                         "content-type" content-type
                         "user-agent" default-user-agent}
        signature (sign-request {:uri (.getPath target-uri)
                                 :request-method :post
                                 :headers request-headers
                                 :body body}
                                headers
                                key-map)]
    (http/post
     inbox
     ;; TODO: timeouts
     {:headers (assoc request-headers "signature" signature)
      :timeout 5000
      :body body}
     (fn send-activity-callback
       [{:keys [error status] :as response}]
       (let [log-msg (format "POST %s to %s completed %s in %.3fms"
                             (:type activity) inbox
                             (or status "with network error")
                             (/ (- (System/nanoTime) start) 1000000.0))]
         (if error
           (log/warn log-msg error)
           (log/info log-msg))
         response)))))

(defn deref-uri
  "Fetch a remote resource, signing the request as a local user.
   This is the way fetching non-public resources is authenticated."
  [{:keys [uri key-map headers content-type]
    :or {headers (remove #(= % "digest") default-signed-headers)
         content-type default-content-type}
    :as options}]
  (let [start (System/nanoTime)
        target-uri (URI. uri)
        request-headers {"host" (.getHost target-uri)
                         "date" (header-time)
                         "content-type" content-type
                         "user-agent" default-user-agent}
        signature (sign-request {:uri (.getPath target-uri)
                                 :request-method :get
                                 :headers request-headers}
                                headers
                                key-map)]
    (http/get
     uri
     {:headers (assoc request-headers "signature" signature)
      :timeout 5000}
     (fn deref-callback
       [{:keys [error status] :as response}]
       (let [log-msg (format "GET %s completed %s in %.3fms"
                             uri (or status "with network error")
                             (/ (- (System/nanoTime) start) 1000000.0))]
         (if error
           (log/warn log-msg error)
           (log/info log-msg))
         response)))))

(defn parse-json
  [input]
  (json/read-value
   input
   (json/object-mapper {:decode-key-fn keyword})))
