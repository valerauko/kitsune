(ns kitsune.fed.inbox.create
  (:require [kitsune.fed.resource :as res]
            [kitsune.logging :as log]))

;; TODO: these inbox handler functions should be standardized
;; and refactored in some more testable fashion (like re-frame reg-events)
(defn create-note
  [remote-account {:keys [object] :as activity}]
  (if (= (:attributedTo object) (:accounts/uri remote-account))
    (if (some->> object (:id) (res/find-resource :note))
      (log/debug ::exists ::note (:id object))
      ;; TODO: figure out how to fetch and store whole convo threads
      ;; idea: if inReplyTo is not empty but it's not in db, then generate
      ;; a random convo id for this post. async go to fetch the replied-to
      ;; post with the convo id in parameter. keep fetching until we reach
      ;; the head of the convo (or a gap). reconcile any convo id conflicts
      ;; (it's just a local id).
      ;; consider: if mastodon returns a replies collection or an ostatus
      ;; conversation id maybe fetch those (too)?
      (res/store object))
    (log/debug ::unauthorized
               ::actor (:accounts/uri remote-account)
               ::object (:uri object)
               ::owner (:attributedTo object))))
