(ns kitsune.fed.outbox.undo
  (:require [clojure.tools.logging :as log]
            [kitsune.db.account :refer [find-by-uri find-local-by-uri]]
            [kitsune.fed.outbox :refer [send-activity]]
            [kitsune.lang :refer [...]]
            [kitsune.uri :refer [url]]
            [kitsune.fed.routes :as-alias fed]))

(defn undo-follow
  [{:keys [actor object]}]
  (if-let [remote-account (find-by-uri object)]
    (if-let [local-account (find-local-by-uri actor)]
      (let [follow-object {:type "Follow"
                           :id (url ::fed/follow
                                    {:follower (:accounts/id local-account)
                                     :followed (:accounts/id remote-account)})
                           :actor (:accounts/uri local-account)
                           :object (:accounts/uri remote-account)}
            undo {:type "Undo"
                  :id (url ::fed/follow-undo
                           {:follower (:accounts/id local-account)
                            :followed (:accounts/id remote-account)})
                  :actor (:accounts/uri local-account)
                  :object follow-object}]
        (send-activity (... local-account remote-account
                            :activity undo)))
      (log/error "Can't send Undo Follow because local account '"
                 actor "' is gone"))
    (log/error "Can't send Undo Follow because remote account '"
               object "' is not in the local database")))
