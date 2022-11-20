(ns kitsune.auth.events
  (:require [cognitect.transit :as t]
            [re-frame.core :as rf]
            [kitsune.effects :as fx]
            [kitsune.http.events :as http]
            [kitsune.routes.events :as route]
            [kitsune.uri :as uri]))

(rf/reg-event-fx
 ::refresh-session
 (fn [state _]
   {:dispatch [::http/load {:path "/auth/v1/session"
                            :opts {:method "PUT"
                                   :credentials "include"}
                            :on-success ::commit-session
                            :on-error ::reset-session}]}))

(rf/reg-event-fx
 ::commit-session
 [fx/persist]
 (fn [{db :db} [_ response]]
   (let [data (js->clj response :keywordize-keys true)]
     {:db (-> db
              (assoc-in [:data :auth] (:token data))
              (assoc-in [:data :user] (:user-id data)))})))

(rf/reg-event-fx
 ::reset-session
 [fx/persist]
 (fn [state [_ response]]
   (let [db (-> (:db state)
                (assoc-in [:data :auth] nil)
                (assoc-in [:data :user] nil))]
     {:db db
      :dispatch [::route/push-state [::uri/welcome]]})))
