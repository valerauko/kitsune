(ns kitsune.auth.subs
  (:require [re-frame.core :as rf]
            [kitsune.subs :as root]))

(rf/reg-sub
 ::auth
 :<- [::root/data]
 :-> :auth)

(rf/reg-sub
 ::user
 :<- [::root/data]
 :-> :user)
