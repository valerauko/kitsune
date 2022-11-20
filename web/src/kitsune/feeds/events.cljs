(ns kitsune.feeds.events
  (:require [clojure.string :as str]
            [kitsune.effects :as fx]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::set-feeds
 [fx/persist]
 (fn [db [_ feeds]]
   ;; TODO: also persist to server/validate feeds etc...
   (let [feeds (cond
                 (set? feeds)
                 (-> feeds (not-empty) (or #{"home"}))

                 (sequential? feeds)
                 (-> feeds (not-empty) (or #{"home"}) (set))

                 (string? feeds)
                 (-> feeds (not-empty) (or "home") (str/split #"\+") (set))

                 :else
                 #{"home"})]
     (assoc-in db [:data :feeds] feeds))))
