(ns kitsune.auth.session.wrappers
  (:require [ring.middleware.session.store :refer [SessionStore]]
            [taoensso.carmine :as car]
            [kitsune.cache :as cache]
            [kitsune.logging :as log])
  (:import [java.util
            UUID]))

(defn new-key
  []
  (->> (UUID/randomUUID)
       (str)
       (.toLowerCase)
       (str "session:")))

(defmethod log/format-line ::failed
  [{::keys [op cache-key result]}]
  (format "Failed to %s session %s (%s)" op cache-key result))

(defrecord CarmineStore []
  SessionStore
  (read-session
    [_ k]
    (-> k (car/get) (cache/ops) first))
  (delete-session
    [_ k]
    (let [[result] (-> k (car/del) (cache/ops))]
      (when-not (= result 1)
        (log/debug ::failed ::op "clear" ::cache-key k ::result result)))
    nil)
  (write-session
    [_ k data]
    (let [k (or k (new-key))
          exp (if (:persistent? data)
                (* 60 60 24 14)
                (* 60 60 24 4))
          [result] (cache/ops
                    (car/setex k exp data))]
      (when-not (= result "OK")
        (log/debug ::failed ::op "update" ::cache-key k ::result result))
      k)))
