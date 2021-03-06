(ns kitsune.wrappers.oauth
  (:require [kitsune.db.oauth :as db]
            [kitsune.db.core :refer [conn]]
            [kitsune.handlers.core :refer [url-decode]]
            [reitit.ring :as reitit]
            [clojure.set :as set]
            [ring.util.http-response :refer [forbidden]]))

(defn bearer-auth
  "Inserts :auth into request with client info from Bearer token."
  [handler]
  (fn [req]
    (let [raw (some->> req :headers :authorization
                           (re-matches #"^Bearer (.+)")
                           second
                           url-decode)
          token (db/find-bearer conn {:token raw})]
      (handler (assoc req :auth token)))))

(defn enforce-scopes
  "Denies requests based on the scopes as defined in the routes"
  [handler]
  (fn [{{:keys [scopes]} :auth :as req}]
    (let [method (:request-method req)
          required (some-> req (reitit/get-match) :data
                               method :scopes)]
      (if (and (seq required)
               (not (set/subset? required (set scopes))))
        (forbidden {:error "Insufficient application scopes"})
        (handler req)))))
