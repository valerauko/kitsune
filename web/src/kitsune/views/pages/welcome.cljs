(ns kitsune.views.pages.welcome
  (:require [shadow.css :refer [css]]
            [re-frame.core :as rf]
            [kitsune.routes.events :as route]
            [kitsune.uri :as uri]
            [kitsune.views.atoms.button :refer [button]]
            [kitsune.views.molecules.label-field :refer [label-field]]
            ["@tabler/icons" :refer [IconAt IconLock IconMail]]))

(def $form
  (css {:display "flex"
        :height "100%"
        :flex-flow "column nowrap"
        :justify-content "center"}))

(def $row
  (css {:display "flex"
        :flex-flow "row nowrap"}))

(def $header
  (css {:font-size "24px"
        :line-height "1.5em"
        :text-align "center"
        :margin-top "24px"
        :margin-bottom "24px"}))

(defn welcome
  [_]
  [:div
   [:h1
    {:class [$header]}
    (str js/window.location.hostname)]
   [:div
    {:class [$row (css :justify-center)]}
    [button
     {:value "Sign up"
      :animated true
      :on-click #(rf/dispatch [::route/push-state [::uri/register]])}]
    [button
     {:primary true
      :animated true
      :value "Log in"
      :on-click #(rf/dispatch [::route/push-state [::uri/login]])}]]])

(defn login
  [_]
  [:form
   {:class [$form]
    :on-submit #(.preventDefault %)}
   [label-field
    {:auto-complete "username"
     :auto-focus true
     :label "Email"
     :max-length 200
     :min-length 5
     :name "email"
     :placeholder "best-fox-ever@vulp.es"
     :prefix [:> IconMail {}]
     :required true
     :type "email"}]
   [label-field
    {:auto-complete "current-password"
     :label "Password"
     :max-length 1000
     :min-length 8
     :name "password"
     :prefix [:> IconLock {}]
     :required true
     :type "password"}]
   [:div
    {:class [$row (css :justify-between)]}
    [button
     {:type "reset"
      :value "Back"
      :on-click #(rf/dispatch [::route/push-state [::uri/welcome]])}]
    [button
     {:primary true
      :animated true
      :type "submit"
      :value "Log in"}]]])

(defn register
  [_]
  [:form
   {:class [$form]
    :on-submit #(.preventDefault %)}
   [label-field
    {:label "Account name"
     :prefix [:> IconAt {}]
     :suffix (str "@" js/window.location.hostname)
     :required true
     :min-length 1
     :max-length 200
     :placeholder "bestfoxever"
     :auto-focus true
     :auto-complete "off"}]
   [label-field
    {:label "Email"
     :prefix [:> IconMail {}]
     :type "email"
     :min-length 5
     :max-length 200
     :required true
     :placeholder "best-fox-ever@vulp.es"
     :auto-complete "username"}]
   [label-field
    {:label "Password"
     :prefix [:> IconLock {}]
     :min-length 8
     :type "password"
     :required true
     :auto-complete "new-password"}]
   [label-field
    {:label "Re-enter password"
     :prefix [:> IconLock {}]
     :min-length 8
     :type "password"
     :required true
     :auto-complete "new-password"}]
   [:div
    {:class [$row (css :justify-between)]}
    [button
     {:type "reset"
      :value "Back"
      :on-click #(rf/dispatch [::route/push-state [::uri/welcome]])}]
    [button
     {:primary true
      :animated true
      :type "submit"
      :value "Sign up"}]]])
