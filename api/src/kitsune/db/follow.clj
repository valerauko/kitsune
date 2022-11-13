(ns kitsune.db.follow
  (:require [kitsune.db :refer [defquery]]
            [kitsune.lang :refer [...]]))

(defquery find-follow
  [{:keys [follower-id followed-id]}]
  {:select [:*]
   :from [:follows]
   :where [:and
           [:= :follower-id follower-id]
           [:= :followed-id followed-id]]
   :limit 1})

(defquery create-follow
  [{:keys [follower-id followed-id]}]
  {:insert-into [:follows]
   :values [(... follower-id followed-id)]})

(defquery delete-follow
  [{:keys [follower-id followed-id]}]
  {:delete-from [:follows]
   :where [:and
           [:= :follower-id follower-id]
           [:= :followed-id followed-id]]
   :limit 1})
