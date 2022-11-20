(ns kitsune.routes.effects
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as easy]))

(rf/reg-fx
 ::push-state
 (fn [route]
   (apply easy/push-state route)))
