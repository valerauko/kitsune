(ns kitsune.auth.session.handlers
  (:refer-clojure :exclude [identity])
  (:require [kitsune.jwt :as jwt])
  (:import [java.time
            ZonedDateTime
            ZoneId]))

(def jwt-keys
  [:user-id])

(def session-time
  (* 60 60 24 14))

(let [utc (ZoneId/of "UTC")]
  (defn two-weeks-later
    []
    (.plusWeeks (ZonedDateTime/now) 2)))

(defn token
  [& maps]
  (jwt/encode (into {} conj maps)))

(defn refresh
  [{{:keys [persistent? user-id] :as session} :session}]
  (if user-id
    {:status 200
     :session session
     :session-cookie-attrs (when persistent? {:expires (two-weeks-later)})
     :body {:token (token {:user-id user-id})
            :user-id user-id}}
    {:status 401}))

(defn login
  [{{{:keys [persistent?]} :body} :parameters}]
  (if-let [user nil]
    {:status 201
     :session (with-meta
                {:user-id (:users/id user)
                 :persistent? (boolean persistent?)}
                {:recreate true})
     :session-cookie-attrs (when persistent? {:expires (two-weeks-later)})
     :body {:token (token {:user-id (:users/id user)})}}
    {:status 404}))

(defn logout
  [{session-id :session/key}]
  {:status 204
   :session nil
   :session/key session-id
   :session-cookie-attrs {:max-age 0}})
