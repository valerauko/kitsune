(ns kitsune.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub
 ::data
 :-> :data)
