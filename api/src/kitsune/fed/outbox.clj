(ns kitsune.fed.outbox
  (:require [clojure.tools.logging :as log]
            [kitsune.db.account :refer [find-private-key]]
            [kitsune.fed.http :as http]
            [kitsune.lang :refer [...]]))

(defn send-activity
  [{:keys [remote-account local-account activity]}]
  (if-let [inbox (or (:accounts/inbox remote-account)
                     (:accounts/shared-inbox remote-account))]
    (let [{:users/keys [private-key]}
          (find-private-key (:accounts/id local-account))]
      (http/send-activity
       (... activity inbox
            :key-map {:key-id (str (:accounts/uri local-account) "#main-key")
                      :pem private-key})))
    (log/error (str "Remote account '"
                    (:accounts/acct remote-account)
                    "' inbox unknown"))))
