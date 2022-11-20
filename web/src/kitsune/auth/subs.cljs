(ns kitsune.auth.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::auth
 :<- :data
 :-> :auth)

(rf/reg-sub
 ::user
 (constantly nil))
