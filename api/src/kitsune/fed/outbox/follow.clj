(ns kitsune.fed.outbox.follow
  (:require [clojure.tools.logging :as log]
            [kitsune.db.account :refer [find-by-uri find-local-by-uri]]
            [kitsune.fed.outbox :refer [send-activity]]
            [kitsune.lang :refer [...]]
            [kitsune.uri :refer [url]]
            [kitsune.fed.routes :as-alias fed]))

(defn follow
  [{:keys [actor object]}]
  (if-let [remote-account (find-by-uri object)]
    (if-let [local-account (find-local-by-uri actor)]
      (let [follow-object {:type "Follow"
                           :id (url ::fed/follow
                                    {:follower (:accounts/id local-account)
                                     :followed (:accounts/id remote-account)})
                           :actor (:accounts/uri local-account)
                           :object (:accounts/uri remote-account)}]
        (send-activity (... local-account remote-account
                            :activity follow-object)))
      (log/error "Can't send Follow because local account '"
                 actor "' is gone"))
    (log/error "Can't send Follow because remote account '"
               object "' is not in the local database")))
