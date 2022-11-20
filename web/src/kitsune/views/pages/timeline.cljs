(ns kitsune.views.pages.timeline
  (:require [clojure.string :as str]
            [shadow.css :refer [css]]
            [re-frame.core :as rf]
            [kitsune.http.events :as http]
            [kitsune.subs :as subs]))

(def $main
  (css {:background "#111"
        :color "#f3f3f0"
        :font "normal normal 16px/1.5em sans-serif"
        :min-height "100vh"
        :display "flex"
        :flex-flow "row nowrap"
        :align-items "flex-start"}))

(def $column
  (css {:margin-top "10px"
        :display "flex"
        :flex-flow "column nowrap"}))

(def $control
  (css {:margin-left "auto"
        :width "350px"
        :border-top-left-radius "6px"
        :border-bottom-left-radius "6px"}))

(def $timeline
  (css {:min-height "70vh"
        :background "#1d1b20"
        :border-radius "6px"
        :margin-left "10px"
        :margin-right "10px"
        :width "600px"}))

(def $secondary
  (css {:margin-right "auto"
        :width "400px"
        :border-top-right-radius "6px"
        :border-bottom-right-radius "6px"}))

(def $control-block
  (css {:margin-bottom "10px"}))

(def $form
  (css {:display "flex"
        :flex-flow "row wrap"}))

;;dark orange 8a3a11
(def $area
  (css {:background "#1d1b20"
        :padding "5px 8px"
        :border-radius "6px"
        :resize "vertical"
        :width "100%"
        :font "normal normal 16px/1.5em sans-serif"
        :min-height "100px"}
       ["&::placeholder"
        {:color "rgba(243, 243, 240, 0.3)"}]
       [:focus
        {:color "#1d1b20"
         :background "#b09d91"
         :outline "none"}]
       ["&:focus::placeholder"
        {:color "rgba(29, 27, 32, 0.5)"}]))

(def $post
  (css {:background "#fe6601"
        :color "#1d1b20"
        :padding "5px 18px"
        :border-radius "6px"
        :margin-top "10px"
        :margin-left "auto"}))

(defn timeline
  [{{{feeds :feeds} :path} :parameters}]
  (let [feeds (-> (or feeds "home") (str/split #"\+"))]
    [:ul
     (map (fn [item] [:li {:key (gensym)} (str "a" item)]) feeds)]))

(defn main
  []
  [:div
   {:class [$main]}
   [:div
    {:class [$column $control]}
    [:section
     {:class [$control-block]}
     [:form
      {:class [$form]}
      [:textarea
       {:class [$area]
        :placeholder "コンコンコン"}]
      [:button
       {:type "submit"
        :class [$post]}
       "コン"]]]
    [:section
     {:class [$control-block]}
     (if-let [auth @(rf/subscribe [::subs/auth])]
       [:p (-> auth (:token) (.split ".") (second) (js/atob) (js/JSON.parse) (pr-str))]
       [:button
        {:on-click #(rf/dispatch [::http/load {:path "/session/v1"
                                               :coords [:auth]
                                               :opts {:method "POST"}}])}
        "LOG IN"])]]
   [:div
    {:class [$column $timeline]}]

   [:div {:class [$column $secondary]}]])
