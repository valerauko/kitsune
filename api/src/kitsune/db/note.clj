(ns kitsune.db.note
  (:require [kitsune.db :refer [defquery]]))

(defquery find-by-uri
  [uri]
  {:select [:*]
   :from [:notes]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery delete-by-uri
  [uri]
  {:delete-from [:notes]
   :where [:= :uri (str uri)]
   :limit 1})

(defquery create
  [note]
  {:insert-into [:notes]
   :values [note]
   :returning [:*]})
