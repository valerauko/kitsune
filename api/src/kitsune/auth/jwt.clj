(ns kitsune.auth.jwt
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
            [buddy.auth.backends :refer [jws]]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.core.keys :as keys]
            [buddy.sign.jwt :as jwt]
            [kitsune.uri :as uri])
  (:import [java.util
            Date]))

(defstate public-key
  :start
  (keys/public-key "keys/jwt.pub"))

(defstate private-key
  :start
  (keys/private-key "keys/jwt.key"))

(def issuer
  uri/host)

(def algo
  :eddsa)

(defn now
  []
  (-> (Date.) (.getTime) (quot 1000)))

(defn encode
  [data]
  (jwt/sign
   data
   private-key
   {:exp (+ (now) 300)
    :iss issuer
    :alg algo}))

(defn decode
  [message]
  (when message
    (try
      (jwt/unsign
       message
       public-key
       {:iss issuer
        :alg algo})
      (catch Throwable ex
        (log/info "JWT error:" (.getMessage ex))
        nil))))

(defstate backend
  :start
  (jws {:token-name "Bearer"
        :secret public-key
        :authfn identity
        :on-error (fn [_ ^Throwable err] (log/debug err (.getMessage err)))
        :options {:iss issuer
                  :alg algo}}))

(defn wrap-jwt
  [handler]
  (fn jwt-wrapper
    [request]
    ((wrap-authentication handler backend) request)))
