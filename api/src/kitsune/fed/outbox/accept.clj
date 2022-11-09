(ns kitsune.fed.outbox.accept
  (:require [clojure.tools.logging :as log]
            [kitsune.db.account :refer [find-by-uri find-local-by-uri]]
            [kitsune.fed.outbox :refer [send-activity]]
            [kitsune.lang :refer [...]]
            [kitsune.uri :refer [url]]
            [kitsune.fed.routes :as-alias fed]))

(defn accept-follow
  "Accept incoming Follow.
   :id      URI of the Follow (it's not a local path)
   :actor   the remote user sending the Follow request
   :object  the local user being Followed"
  [{:keys [id actor object]}]
  (if-let [remote-account (find-by-uri actor)]
    (if-let [local-account (find-local-by-uri object)]
      (let [accept {:type "Accept"
                    :id (url ::fed/follow-accept
                             {:follower (:accounts/id remote-account)
                              :followed (:accounts/id local-account)})
                    :actor (:accounts/uri local-account)
                    :object id}]
        (send-activity (... local-account remote-account :activity accept)))
      (log/error "Can't send Accept because local account '"
                 object "' is gone"))
    (log/error "Can't send Accept because remote account '"
               actor "' is not in the local database")))
