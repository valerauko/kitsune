(ns kitsune.fed.inbox
  (:require [clojure.tools.logging :as log]
            [clojure.pprint :refer [pprint]]
            [jsonista.core :as json]
            [csele.headers :as headers]
            [kitsune.fed.conversions :as conv]
            [kitsune.fed.http :as http]
            [kitsune.fed.inbox.follow :refer [follow]]
            [kitsune.db.account :refer [find-by-uri upsert]]
            [kitsune.lang :refer [...]]))

(defn parse-json
  [input]
  (json/read-value
   input
   (json/object-mapper {:decode-key-fn keyword})))

(defn fetch-and-store
  [uri]
  (let [{:keys [status body]} @(http/fetch-resource uri)]
    (when (<= 200 status 299)
      (let [{:keys [type] :as object} (parse-json body)]
        (log/debug (str uri " is " type))
        (case type
          ("Person" "Service")
          (some-> object (conv/person->account) (upsert))

          ;; else
          object)))))

(defn check-sig
  "Checks HTTP header signature of the activity's request.
   Refetches the user's public key if it doesn't work at first try.
   Depending on the user's instance that request can be pretty slow."
  [{{{sig-header "signature"} :headers
     {{actor :actor} :body} :parameters
     :as request} :request}]
  ; first see if the key in the db (if any) can validate the sig
  (let [{:accounts/keys [public-key]} (find-by-uri actor)]
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
         (empty? (find-by-uri actor)))
    (log/debug (str "Ignoring Delete of unknown user " object))

    (check-sig {:request request})
    (let [remote-account (find-by-uri (or (:id actor) actor))]
      (log/debug (str "Validated " type " (" id ")"))
      (case type
        "Accept"
        (kitsune.lang/inspect type id body-params)

        ; "Add"

        "Announce"
        (kitsune.lang/inspect type id body-params)

        ; "Block"

        "Create"
        (kitsune.lang/inspect type id body-params)

        "Delete"
        (kitsune.lang/inspect type id body-params)

        ; "Flag"

        "Follow"
        (follow (... id object remote-account))

        "Like"
        (kitsune.lang/inspect type id body-params)

        ; "Move"

        ; "Read"

        "Reject"
        (kitsune.lang/inspect type id body-params)

        "Undo"
        (kitsune.lang/inspect type id body-params)

        ; "Update"

        ;; else
        (log/warn "Unhandled activity type" type)))

    :else
    (log/debug (str "Couldn't validate " type " (" id ")"))))
