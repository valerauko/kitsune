(ns kitsune.views.molecules.label-field
  (:require [shadow.css :refer [css]]
            [kitsune.views.atoms.input-field :refer [input-field]]))

(def $label
  (css {:padding "0 15px 10px 15px"
        :display "inline-block"}))

(def $wrap
  (css {:margin-bottom "10px"}))

(defn label-field
  [{:keys [label class] :as props}]
  [:label
   {:class [$wrap]}
   [:span
    {:class [$label]}
    label]
   [input-field
    (dissoc props :label)]])
