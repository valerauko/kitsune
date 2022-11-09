(ns kitsune.db.account
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

(defquery upsert
  [account]
  {:replace-into :accounts
   :values [account]
   :returning [:*]})
