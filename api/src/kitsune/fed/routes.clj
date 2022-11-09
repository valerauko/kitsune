(ns kitsune.fed.routes
  (:require [kitsune.fed.handlers :as handlers]))

(def routes
  ["/api/fed"
   ["/v1"
    ["/account/:name"
     {:parameters {:path {:name string?}}}
     [""
      {:name ::account-show
       :get {:handler handlers/show-account}}]
     ["/followers" ::account-followers]
     ["/following" ::account-following]
     ["/inbox"
      {:name ::account-inbox
       :parameters {:body [:map
                           {:closed false}
                           [:id string?]
                           [:type string?]
                           [:object [:or string? map?]]
                           [:actor [:or string? map?]]]}
       :post {:handler handlers/inbox}}]
     ["/outbox" ::account-outbox]]
    ["/follow/:follower/:followed"
     {:parameters {:path {:follower int?
                          :followed int?}}}
     ["" {:name ::follow}]
     ["/accept" {:name ::follow-accept}]
     ["/reject" {:name ::follow-reject}]
     ["/undo" {:name ::follow-undo}]]
    ["/note/:id"
     {:parameters {:path {:id int?}}}
     ["" {:name ::note-show}]]
    ["/authorize_follow"
     {:name ::authorize-follow
      :parameters {:query {:acct :string}}}]
    ["/inbox"
     {:name ::shared-inbox
      :parameters {:body [:map
                          {:closed false}
                          [:id string?]
                          [:type string?]
                          [:object [:or string? map?]]
                          [:actor [:or string? map?]]]}
      :post {:handler handlers/inbox}}]]])
