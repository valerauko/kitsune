(ns kitsune.fed.handlers
  (:require [clojure.walk :refer [keywordize-keys]]
            [ring.util.http-response :refer [ok not-found]]
            [kitsune.async :refer [delayed]]
            [kitsune.cache :as cache]
            [kitsune.db.account :refer [find-local-account]]
            [kitsune.uri :refer [url]]
            [kitsune.fed.inbox :as inbox]
            [kitsune.fed.routes :as-alias routes])
  (:import [java.util
            Date]))

(def common-context
  {(keyword "@context") ["https://www.w3.org/ns/activitystreams",
                         "https://w3id.org/security/v1"]})

(defn show-account
  [{{{name :name} :path} :parameters}]
  (if-let [{:accounts/keys [display-name inbox name public-key uri]}
           (find-local-account name)]
    (let [api-url (url ::routes/account-show {:name name})]
      (ok
       (merge
        common-context
        {:type "Person"
         :id api-url
         :url uri
         :name display-name
         :preferredUsername name
         :following (url ::routes/account-following {:name name})
         :followers (url ::routes/account-followers {:name name})
         :inbox inbox
         :outbox (url ::routes/account-outbox {:name name})
         :endpoints {:sharedInbox (url ::routes/shared-inbox)}
         :publicKey {:id (str api-url "#main-key")
                     :owner api-url
                     :publicKeyPem public-key}})))
    (not-found)))

(defn serialize-request
  [req]
  (-> req
      (select-keys [:server-port :server-name :remote-addr :uri :scheme :headers
                    :protocol :request-method :parameters])
      (assoc :body (.bytes (:body req)))))

(defn inbox
  [{{{:keys [id]} :body} :parameters :as req}]
  (when-not (cache/get id)
    (cache/set id (Date.))
    (delayed (inbox/handle-activity (serialize-request req))))
  {:status 206})
