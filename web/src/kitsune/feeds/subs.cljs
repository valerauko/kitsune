(ns kitsune.feeds.subs
  (:require [re-frame.core :as rf]
            [kitsune.subs :as root]))

(rf/reg-sub
 ::feeds
 :<- [::root/data]
 :-> :feeds)
