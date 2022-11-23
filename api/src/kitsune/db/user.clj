(ns kitsune.db.user
  (:require [kitsune.db :refer [defquery]]))

(defquery find-by-email
  [email]
  {:select [:id :email :password]
   :from [:users]
   :where [:= :email (str email)]
   :limit 1})
