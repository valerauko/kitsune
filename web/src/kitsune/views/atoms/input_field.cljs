(ns kitsune.views.atoms.input-field
  (:require [shadow.css :refer [css]]))

(def $wrap
  (css {:display "flex"
        :flex-flow "row no-wrap"
        :border "1px solid #b09d91"
        :border-radius "6px"}
       [:focus-within
        {:border "1px solid #fe6601"
          :outline "1px solid #fe6601"}]))

(def $fix
  (css {:min-width "15px"
        :padding "10px 15px"}))

(def $prefix
  (css {:padding-right "10px"}))

(def $suffix
  (css {:padding-left "10px"}))

(def $field
  (css {:background "inherit"
        :padding-top "10px"
        :padding-bottom "10px"
        :width "150px"
        :flex-grow "1"}
       [:focus
        {:outline "none"}]))

(defn input-field
  [{:keys [prefix suffix type] :as opts}
   {:or {prefix "" suffix "" type "text"}}]
  (let [field-opts (into {:class [$field]
                          :type type}
                         (dissoc opts :class :type))]
    [:div
     {:class [$wrap]}
     [:div
      {:class [$fix $prefix]}
      prefix]
     [:input
      field-opts]
     [:div
      {:class [$fix $suffix]}
      suffix]]))
