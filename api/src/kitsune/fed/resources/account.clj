(ns kitsune.fed.resources.account
  (:refer-clojure :exclude [find])
  (:require [kitsune.db.account :as account]
            [kitsune.fed.conversions :as conv]
            [kitsune.fed.resource :refer [-find-resource -store-resource]]))

(defn store
  [res]
  (->> res
       (conv/person->account)
       (account/upsert)))

(defn find
  [id]
  (account/find-by-uri id))

(defmethod -store-resource :person
  -store-person
  [res]
  (store res))

(defmethod -store-resource :service
  -store-service
  [res]
  (store res))

(defmethod -find-resource :person
  -find-person
  [_ id]
  (find id))

(defmethod -find-resource :service
  -find-person
  [_ id]
  (find id))
