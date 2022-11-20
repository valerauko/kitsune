(ns kitsune.jwt
  (:require [clojure.tools.logging :as log]
            [mount.core :refer [defstate]]
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
    :alg :eddsa}))

(defn decode
  [message]
  (when message
    (try
      (jwt/unsign
       message
       public-key
       {:iss issuer
        :alg :eddsa})
      (catch Throwable ex
        (log/info "JWT error:" (.getMessage ex))
        nil))))
