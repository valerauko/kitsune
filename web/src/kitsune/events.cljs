(ns kitsune.events
  (:require [re-frame.core :as rf]
            [kitsune.effects :as fx]))

(rf/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx ::fx/persisted)]
 (fn [{:keys [persisted]}]
   {:db {:days persisted}}))
