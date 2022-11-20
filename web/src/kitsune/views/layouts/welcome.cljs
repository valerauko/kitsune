(ns kitsune.views.layouts.welcome
  (:require [shadow.css :refer [css]]))

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

(defn view
  ([child]
   (view {} child))
  ([_ & children]
   [:div
    {:class [$wrap]}
    (into
     [:div
      {:class [$panel]}]
     children)]))
