(ns kitsune.fed.inbox.follow
  (:require [kitsune.async :refer [delayed]]
            [kitsune.db.account :refer [find-local-by-uri]]
            [kitsune.fed.outbox.accept :refer [accept-follow]]
            [kitsune.lang :refer [...]]))

(defn follow
  [{:keys [id object remote-account]}]
  (when-let [local-account (find-local-by-uri object)]
    ;; store follow in db
    ;; send accept if local auto-accepts follows
    (delayed (accept-follow (... id object
                                 :actor (:accounts/uri remote-account))))))
