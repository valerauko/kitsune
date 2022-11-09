(ns kitsune.fed.inbox
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [csele.headers :as headers]
            [kitsune.cache :as cache]
            [kitsune.fed.conversions :as conv]
            [kitsune.fed.http :as http]
            [kitsune.fed.inbox.follow :refer [follow]]
            [kitsune.fed.inbox.delete :refer [delete]]
            [kitsune.db.account :refer [find-by-uri upsert]]
            [kitsune.lang :refer [...]]))

(defn fetch-and-store
  [uri]
  (let [{:keys [status body]} @(http/fetch-resource uri)]
    (when (<= 200 status 299)
      (let [{:keys [type] :as object} (http/parse-json body)]
        (log/debug (str uri " is " type))
        (case type
          ("Person" "Service")
          (some->> object
                   (conv/person->account)
                   (upsert)
                   (into {})
                   (cache/set uri))

          ;; else
          object)))))

(defn find-actor
  [actor]
  (let [actor-id (or (:id actor) actor)]
    (or (cache/get actor-id)
        (some->> actor-id
                 (find-by-uri)
                 (into {})
                 (cache/set actor-id)))))

(defn check-sig
  "Checks HTTP header signature of the activity's request.
   Refetches the user's public key if it doesn't work at first try.
   Depending on the user's instance that request can be pretty slow."
  [{{{sig-header "signature"} :headers
     {{actor :actor} :body} :parameters
     :as request} :request}]
  ; first see if the key in the db (if any) can validate the sig
  (let [{:accounts/keys [public-key]} (find-actor actor)]
    (if (and public-key (headers/verify request public-key))
      true
      ; if not then refetch the actor's key and use that to validate
      (some->> actor
               (fetch-and-store)
               :accounts/public-key
               (headers/verify request)))))

(defn handle-activity
  [{{{:keys [actor id object type] :as body-params} :body} :parameters
    {ua "user-agent"} :headers
    :as request}]
  (log/debug (str "Received " type " (" id ") via " ua))
  (cond
    ;; if the object of the Delete is the actor then it's an account deletion
    ;; and if the account isn't saved locally there's no key to verify the
    ;; request either (since the user is deleted their key is gone too)
    ;; so it'd be a waste to try to fetch it over http, just ignore
    (and (= type "Delete")
         (or (= object actor)
             (and (= (:type object) "Tombstone")
                  (= (:id object) actor)))
         (empty? (find-actor actor)))
    (log/debug (str "Ignoring Delete of unknown user " object))

    ;; actor can be an array too...
    ;; https://www.w3.org/TR/activitystreams-vocabulary/#dfn-actor
    (check-sig {:request request})
    (let [remote-account (find-actor actor)]
      ;; consider how to deal with it if the actor is local actually
      (case type
        "Accept"
        (kitsune.lang/inspect type id body-params)

        "Add"
        (kitsune.lang/inspect type id body-params)

        "Announce"
        (kitsune.lang/inspect type id body-params)

        "Block"
        (kitsune.lang/inspect type id body-params)

        "Create"
        (kitsune.lang/inspect type id body-params)

        "Delete"
        (delete body-params)

        "Flag"
        (kitsune.lang/inspect type id body-params)

        "Follow"
        (follow (... id object remote-account))

        "Like"
        (kitsune.lang/inspect type id body-params)

        "Move"
        (kitsune.lang/inspect type id body-params)

        "Read"
        (kitsune.lang/inspect type id body-params)

        "Reject"
        (kitsune.lang/inspect type id body-params)

        "Undo"
        (kitsune.lang/inspect type id body-params)

        "Update"
        (kitsune.lang/inspect type id body-params)

        ;; else
        (log/warn "Unhandled activity type" type)))

    :else
    (log/debug (str "Couldn't validate " type " (" id ")"))))
