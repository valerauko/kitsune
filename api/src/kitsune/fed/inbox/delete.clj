(ns kitsune.fed.inbox.delete
  (:require [clojure.tools.logging :as log]
            [kitsune.db.account :as account]
            [kitsune.db.note :as note]
            [kitsune.lang :refer [cond-let]]))

(defn delete
  [{{actor :accounts/uri actor-id :accounts/id} :account
    :keys [object]
    :as activity}]
  (if-let [object-id (cond
                       (string? object) object
                       (map? object) (:id object))]
    (cond-let
     [{owner :notes/account-id} (note/find-by-uri object-id)]
     (if (= owner owner)
       (note/delete-by-uri object-id)
       (log/info (str "Actor '" actor "' can't delete note '" object-id)))

     [{account :accounts/uri} (account/find-by-uri object-id)]
     (if (= account actor)
       (account/delete-by-uri account)
       (log/debug (str "Actor '" actor "' can't delete account '" account)))

     :else
     (log/debug (str "Object '" object-id "' not found")))
    (log/debug "Unidentifiable object" object)))
