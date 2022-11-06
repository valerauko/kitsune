(ns kitsune.fed.inbox
  (:require [clojure.tools.logging :as log]
            [jsonista.core :as json]
            [csele.headers :as headers]
            [kitsune.fed.http :as http]
            [kitsune.db.account :refer [find-by-uri]]))

(defn parse-json
  [input]
  (json/read-value
   input
   (json/object-mapper {:decode-key-fn keyword})))

(defn tap
  [tag thing]
  (log/debug tag thing)
  thing)

(defn check-sig
  "Checks HTTP header signature of the activity's request.
   Refetches the user's public key if it doesn't work at first try.
   Depending on the user's instance that request can be pretty slow."
  [{{{sig-header "signature"} :headers
     {actor :actor} :body-params
     :as request} :request}]
  ; first see if the key in the db (if any) can validate the sig
  (let [{:accounts/keys [public-key]} (find-by-uri actor)]
    (if (and public-key (headers/verify request public-key))
      true
      ; if not then refetch the actor's key and use that to validate
      (let [refetched-actor @(http/fetch-resource actor)]
        (some->> refetched-actor
                 :body
                 (parse-json)
                 :publicKey
                 :publicKeyPem
                 (headers/verify request))))))

(defn handle-activity
  [{{{:keys [actor id object type]} :body} :parameters
    {ua "user-agent"} :headers
    :as request}]
  (log/debug (str "Received " type " (" id ") via " ua))
  (cond
    ;; if the subject of the Delete is the actor then it's an account deletion
    ;; and if the account isn't saved locally there's no key to verify the
    ;; request either (since the user is deleted their key is gone too)
    ;; so it'd be a waste to try to fetch it over http, just ignore
    (and (= type "Delete")
         (= object actor)
         (empty? (find-by-uri (str object))))
    (log/debug (str "Ignoring Delete of unknown user " object))

    (check-sig {:request request})
    (log/debug (str "Validated " type " (" id ")"))

    :else
    (log/debug (str "Couldn't validate " type " (" id ")"))))
