(ns kitsune.fed.inbox.follow
  (:require [kitsune.async :refer [delayed]]
            [kitsune.db.account :refer [find-local-by-uri]]
            [kitsune.db.follow :refer [create-follow find-follow]]
            [kitsune.fed.outbox.accept :refer [accept-follow]]
            [kitsune.lang :refer [...]]))

(defn follow
  [{:keys [id object]
    {follower-id :accounts/id :as remote-account} :remote-account}]
  (when-let [{followed-id :accounts/id} (find-local-by-uri object)]
    (when-not (find-follow (... follower-id followed-id))
      (create-follow (... follower-id followed-id)))
    ;; TODO: only send accept if local auto-accepts follows
    (delayed (accept-follow (... id object
                                 :actor (:accounts/uri remote-account))))))
