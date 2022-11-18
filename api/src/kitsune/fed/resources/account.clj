(ns kitsune.fed.resources.account
  (:refer-clojure :exclude [find])
  (:require [kitsune.db.account :as account]
            [kitsune.fed.conversions :as conv]
            [kitsune.fed.resource :as res
             :refer [-find-resource -store-resource]]))

(defn store
  [res]
  (->> res
       (conv/person->account)
       (account/upsert)))

(defn find
  [id]
  (account/find-by-uri id))

(derive ::res/person ::res/account)
(derive ::res/service ::res/account)

(defmethod -store-resource ::res/account
  -store-person
  [res]
  (->> res
       (conv/person->account)
       (account/upsert)))

(defmethod -find-resource ::res/account
  -find-person
  [_ id]
  (account/find-by-uri id))
