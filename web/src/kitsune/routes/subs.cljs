(ns kitsune.routes.subs
  (:require [re-frame.core :as rf]
            [kitsune.routes :as-alias routes]))

(rf/reg-sub
 ::current-route
 (fn [db]
   (::routes/current-route db)))
