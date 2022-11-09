(ns kitsune.db.account
  (:refer-clojure :exclude [update])
  (:require [next.jdbc :refer [with-transaction]]
            [kitsune.db :as db :refer [defquery]]))

(defquery find-local-account
  [name]
  {:select [:accounts.*]
   :from [:accounts]
   :inner-join [:users [:= :accounts.id :users.account-id]]
   :where [:= :accounts.name (str name)]
   :limit 1})

(defquery find-local-by-name
  [name]
  {:select [:accounts.uri :accounts.name :accounts.acct]
   :from [:accounts]
   :inner-join [:users [:= :accounts.id :users.account-id]]
   :where [:= :accounts.name (str name)]
   :limit 1})

(defquery find-by-uri
  [uri]
  {:select [:*]
   :from [:accounts]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery find-local-by-uri
  [uri]
  {:select [:accounts.*]
   :from [:accounts]
   :inner-join [:users [:= :accounts.id :users.account-id]]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery find-private-key
  [account-id]
  {:select [:private-key]
   :from [:users]
   :where [:= :account-id account-id]
   :limit 1})

(defquery select-for-update
  [uri]
  {:select [:*]
   :for :update
   :from [:accounts]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery update
  [{:keys [uri] :as account}]
  (let [update-keys (keys (dissoc account :id :uri :created-at :updated-at))]
    {:update [:accounts]
     :set (select-keys account update-keys)
     :where [:= :uri uri]
     :limit 1}))

(defquery create
  [account]
  {:insert-into [:accounts]
   :values [account]
   :returning [:*]})

(defn upsert
  [{:keys [uri] :as account}]
  (with-transaction [tx db/datasource]
    (if-let [local (select-for-update uri)]
      (do
        (update account)
        (find-by-uri uri))
      (create account))))
