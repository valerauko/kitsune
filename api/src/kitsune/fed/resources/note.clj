(ns kitsune.fed.resources.note
  (:require [clojure.instant :refer [read-instant-date]]
            [kitsune.db.account :as account]
            [kitsune.db.note :as note]
            [kitsune.fed.resource :as res
             :refer [find-or-fetch -find-resource -store-resource]]))

(defmethod -find-resource ::res/note
  -find-note
  [_ uri]
  (note/find-by-uri uri))

(defmethod -store-resource ::res/note
  -store-note
  [{owner-id :attributedTo :as object}]
  (when-let [owner (find-or-fetch ::res/account owner-id)]
    (let [{uri :id url :url text :content spoiler :summary
           :or {url uri}} object]
      (kitsune.lang/inspect object)
      (note/create
       {:uri (not-empty (str uri))
        :url (not-empty (str url))
        :content (not-empty (str text))
        :spoiler (not-empty (str spoiler))
        :account-id (:accounts/id owner)
        :published-at (some-> object :published read-instant-date)}))))
