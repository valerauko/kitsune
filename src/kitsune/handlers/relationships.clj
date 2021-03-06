(ns kitsune.handlers.relationships
  (:require [kitsune.db.relationship :as db]
            [kitsune.db.user :as users]
            [kitsune.db.core :refer [conn]]
            [kitsune.uri :as uri]
            [kitsune.handlers.core :refer [defhandler]]
            [kitsune.federators.follow :as fed]
            [ring.util.http-response :refer :all])
  (:import [java.util UUID]))

(defn present?
  [val]
  (if val true false))

; TODO: move to presenter
(defn relationship
  [& {:keys [subject object]}]
  {:id subject
   :following (present? (db/follows? conn {:subject subject :object object}))
   :followed-by (present? (db/follows? conn {:subject object :object subject}))
   ; TODO
   :blocking false
   :muting false
   :muting-notifications false
   :requested (present? (db/requested-follow? conn {:followed object :follower subject}))
   :domain-blocking false
   :showing-reblogs true})

(defhandler follow
  [{{followed :id} :path-params
    {follower :user-id} :auth}]
  ; TODO: showing reblogs, blocking
  ; TODO: proper error messages
  ; reasons this might fail:
  ; - subject's not logged in (not null violation)
  ; - object doesn't exist (foreign key violation)
  ; - subject already follows object (unique constraint violation)
  (let [current-user (users/find-by-user-id conn {:id follower})
        object (users/find-by-id conn {:id followed})
        follow-uri (str (uri/url "/follow/" (UUID/randomUUID)))
        accept-uri (if (and (:local object) (not (:approves-follow object)))
                     (str (uri/url "/accept/" (UUID/randomUUID))))]
    (if-let [record (db/follow! conn {:uri follow-uri
                                      :followed (:account-id object)
                                      :follower (:account-id current-user)
                                      :accept-uri accept-uri})]
      (if-not (:local object)
        (fed/send-follow {:uri follow-uri
                          :followed object
                          :follower current-user})))
    (ok (relationship :subject (:account-id current-user)
                      :object (:account-id object)))))

(defhandler unfollow
  [{{followed :id} :path-params
    {follower :user-id} :auth}]
  (let [current-user (users/find-by-user-id conn {:id follower})
        object (users/find-by-id conn {:id followed})]
    (if-let [record (db/unfollow! conn {:followed (:account-id object)
                                        :follower (:account-id current-user)})]
      (if-not (:local object)
        (fed/send-undo {:uri (:uri record)
                        :followed object
                        :follower current-user})))
    (ok (relationship :subject (:account-id current-user)
                      :object (:account-id object)))))
