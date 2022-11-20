(ns kitsune.views.pages.login
  (:require [shadow.css :refer [css]]))

(def $form
  (css {:display "flex"
        :flex-flow "row wrap"}))

(def $input
  (css {:border-radius "6px"
        :background "#2d2929"
        :padding "10px 15px"}
       [:focus
        {:outline "1px solid #b09d91"}]))

(def $field
  (css {:width "100%"
        :margin-bottom "10px"}))

(def $signup
  (css {:margin-left "auto"
        :margin-right "10px"}))

(def $login
  (css {:background "#fe6601"
        :color "#1d1b20"}))

(defn view
  [_]
  [:form
   {:class [$form]}
   [:input
    {:class [$input $field]
     :type "email"
     :required true
     :placeholder "bestfoxever@vulp.es"
     :auto-focus true
     :auto-complete "username"}]
   [:input
    {:class [$input $field]
     :type "password"
     :required true
     :auto-complete "current-password"}]
   [:input
    {:class [$input $signup]
     :type "button"
     :value "Sign up"}]
   [:input
    {:class [$input $login]
     :type "submit"
     :value "Log in"}]])
