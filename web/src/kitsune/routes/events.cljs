(ns kitsune.routes.events
  (:require [re-frame.core :as rf]
            [kitsune.routes :as-alias routes]
            [kitsune.routes.effects :as fx]
            [reitit.frontend.controllers :as router]))

(rf/reg-event-fx
 ::push-state
 (fn [_ [_ route]]
   {::fx/push-state route}))

(rf/reg-event-fx
 ::pop-state
 (fn [_ _]
   {::fx/pop-state {}}))

(rf/reg-event-db
 ::navigated
 (fn [db [_ new-match]]
   (let [old-match (::routes/current-route db)]
     (if-let [old-controllers (:controllers old-match)]
       (let [controllers (router/apply-controllers
                          (:controllers old-match)
                          new-match)]
         (assoc db ::routes/current-route
                        (assoc new-match :controllers controllers)))
       (assoc db ::routes/current-route new-match)))))
