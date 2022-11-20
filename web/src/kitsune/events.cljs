(ns kitsune.events
  (:require [re-frame.core :as rf]
            [kitsune.auth.events :as auth.events]
            [kitsune.effects :as fx]))

(rf/reg-event-fx
 ::initialize-db
 [(rf/inject-cofx ::fx/persisted)]
 (fn [{:keys [persisted]}]
   {:db {:data persisted}
    :dispatch [::auth.events/refresh-session]}))
