(ns kitsune.fed.inbox.undo
  (:require [kitsune.db.boost :as boost]
            [kitsune.logging :as log]))

(defn undo-announce
  [remote-account activity]
  (let [object-id (get-in activity [:object :id]
                          (:object activity))]
    (if-let [boost (boost/find-by-uri object-id)]
      (if (= (:accounts/id remote-account) (:boosts/account-id boost))
        (boost/delete-by-uri object-id)
        (log/debug ::unauthorized
                   ::actor (:accounts/id remote-account)
                   ::object object-id
                   ::owner (:boosts/account-id boost)))
      (log/debug ::missing ::id object-id))))
