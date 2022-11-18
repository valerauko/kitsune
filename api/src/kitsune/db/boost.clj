(ns kitsune.db.boost
  (:require [kitsune.db :refer [defquery]]))

(defquery find-by-uri
  [uri]
  {:select [:*]
   :from [:boosts]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery delete-by-uri
  [uri]
  {:delete-from [:boosts]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery create
  [boost]
  {:insert-into [:boosts]
   :values [boost]
   :returning [:*]})
