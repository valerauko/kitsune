(ns kitsune.fed.inbox
  (:require [kitsune.logging :as log]
            [com.brunobonacci.mulog :as u]
            [clojure.pprint :refer [pprint]]
            [csele.headers :as headers]
            [kitsune.cache :as cache]
            [kitsune.fed.conversions :as conv]
            [kitsune.fed.http :as http]
            [kitsune.fed.inbox.create :refer [create-note]]
            [kitsune.fed.inbox.follow :refer [follow]]
            [kitsune.fed.inbox.delete :refer [delete]]
            [kitsune.db.account :refer [find-by-uri upsert]]
            [kitsune.lang :refer [...]]))

(defn fetch-and-store
  [uri]
  (let [{:keys [status body]} @(http/fetch-resource uri)]
    (when (and status (<= 200 status 299))
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

(defmethod log/format-line ::receive
  [{::keys [ua] {:keys [id type]} ::activity}]
  ;; Received Delete (https://mstdn.io/users/dtoshi#delete) via http.rb/5.1.0 (Mastodon/4.0.2; +https://mstdn.io/)
  (format "Received %s (%s) via %s" type id ua))

(defmethod log/format-line ::unknown
  [{::keys [activity object]}]
  ;; Ignoring Delete (https://mstdn.io/users/dtoshi#delete) of Person (https://mstdn.io/users/dtoshi)
  (format "Ignoring %s (%s) of %s" (:type activity) (:id activity) (:id object)))

(defn object-map
  [object]
  (cond
    (string? object) {:id object}
    (map? object) (select-keys object [:id :type])
    :else {:id object}))

(defn handle-activity
  [{{{:keys [actor id object type] :as body-params} :body} :parameters
    {ua "user-agent"} :headers
    :as request}]
  (u/with-context {::activity (... type id)
                   ::actor (or (:id actor) actor)}
    (log/info ::receive ::ua ua)
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
      (log/trace ::unknown ::object (object-map object))

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
          (if (= (:type object) "Note")
            (create-note remote-account body-params)
            (log/debug ::unknown ::object (object-map object)))

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
          (log/warn ::unknown ::object (object-map object))))

      :else
      (log/debug ::invalid))))
