(ns kitsune.views.atoms.button
  (:require [shadow.css :refer [css]]))

(def $wrap
  (css {:margin "10px"
        :z-index "10"}))

(def $basic
  (css {:--background-color "#1d1b20"
        :--border-color "#b09d91"
        :color "#f3f3f0"}
       [:focus
        {:border "1px solid #fe6601"
         :outline "1px solid #fe6601"}]))

(def $primary
  (css {:--background-color "#fe6601"
        :--border-color "#fe6601"
        :color "#1d1b20"}
       [:focus
        {:border "1px solid #fe6601"
         :outline "none"}]))

(def $input
  (css {:cursor "pointer"
        :z-index "10"
        :border-radius "6px"
        :min-width "100px"
        :border "1px solid var(--border-color, inherit)"
        :outline "none"
        :background "var(--background-color, inherit)"
        :padding "10px 15px"}
       [:hover
        {:filter "opacity(0.9)"}]))

(def $wrap-animated
  (css {:width "min-content"
        :position "relative"}
       ["&:hover::after, &:hover::before"
        {:z-index "0"
         :position "absolute"
         :content "\"\""
         :top "-4px"
         :left "-4px"
         :bottom "-4px"
         :right "-4px"
         :border "2px solid var(--spin-color, var(--border-color, inherit))"
         :animation "clippath 3s infinite linear"
         :border-radius "8px"}]
       ["&:hover::after"
        {:animation "clippath 3s infinite -1.5s linear"}]))

(defn button
  [{:keys [primary animated type] classes :class :as opts
    :or {classes []}}]
  (let [style (if primary $primary $basic)]
    [:div
     {:class (into (into [$wrap style] classes)
                   (if animated [$wrap-animated] []))}
     [:input
      (into
       {:class [style $input]
        :type (or type "button")}
       (dissoc opts :animated :class :primary :type))]]))
