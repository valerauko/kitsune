(ns kitsune.db.account
  (:require [kitsune.db :refer [defquery]]))

(defquery find-local-account
  [name]
  {:select [:accounts.*]
   :from [:accounts]
   :inner-join [:users [:= :accounts.id :users.account-id]]
   :where [:= :accounts.name name]
   :limit 1})

(defquery find-local-by-name
  [name]
  {:select [:accounts.id :accounts.name :accounts.acct]
   :from [:accounts]
   :inner-join [:users [:= :accounts.id :users.account-id]]
   :where [:= :accounts.name name]
   :limit 1})

(defquery find-by-uri
  [uri]
  {:select [:*]
   :from [:accounts]
   :where [:= :uri uri]
   :limit 1})
