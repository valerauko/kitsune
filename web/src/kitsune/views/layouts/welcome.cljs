(ns kitsune.views.layouts.welcome
  (:require [shadow.css :refer [css]]
            [re-frame.core :as rf]
            [kitsune.routes.events :as route]
            [kitsune.uri :as uri]))

(def $wrap
  (css {:width "100%"
        :height "100vh"
        :background "#8a3a11"
        :display "flex"
        :align-items "center"}))

(def $panel
  (css {:width "500px"
        :height "600px"
        :margin "auto"
        :background "#1d1b20"
        :color "#f3f3f0"
        :padding "15px"
        :border-radius "8px"
        :box-shadow "0 0 5px black"
        :display "flex"
        :flex-flow "column nowrap"}))

(def $logo
  (css {:margin "-75px auto 0 auto"
        :background-color "#1d1b20"
        :background-image "url(\"/icon.png\")"
        :background-size "80% 80%"
        :background-position "center center"
        :background-repeat "no-repeat"
        :box-shadow "0 0 5px black"
        :border-radius "50%"
        :height "120px"
        :min-height "120px"
        :cursor "pointer"
        :width "120px"}))

(defn view
  ([child]
   (view {} child))
  ([_ & children]
   [:div
    {:class [$wrap]}
    (into
     [:div
      {:class [$panel]}
      [:div
       {:class [$logo]
        :on-click #(rf/dispatch [::route/push-state [::uri/welcome]])}]]
     children)]))
