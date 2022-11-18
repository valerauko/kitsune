(ns kitsune.fed.inbox.announce
  (:require [clojure.instant :refer [read-instant-date]]
            [kitsune.db.boost :as boost]
            [kitsune.fed.resource :as res]
            [kitsune.logging :as log]))

(defn announce-note
  [remote-account {:keys [published id] object-id :object}]
  (if-let [note (res/find-or-fetch :note object-id)]
    (boost/create {:uri id
                   :account-id (:accounts/id remote-account)
                   :note-id (:notes/id note)
                   :published-at (some-> published read-instant-date)})
    (log/debug ::missing ::id object-id)))
