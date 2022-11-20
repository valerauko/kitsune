(ns kitsune.views
  (:require [shadow.css :refer [css]]
            [re-frame.core :as rf]
            [kitsune.auth.scopes :as scope]
            [kitsune.auth.subs :as auth]
            [kitsune.routes.events :as event]
            [kitsune.routes.subs :as route]
            [kitsune.uri :as uri]))

(def $main
  (css {:font "normal normal 16px/1.5em sans-serif"}))

(defn main
  []
  [:div
   {:class [$main]}
   (let [{{:keys [layout redirect-to scope view]} :data :as route}
         @(rf/subscribe [::route/current-route])
         user @(rf/subscribe [::auth/user])
         wrapped-view (if layout
                        [layout route
                         [view route]]
                        [view route])]
     (cond
       redirect-to
       (rf/dispatch [::event/push-state redirect-to])

       (not (fn? view))
       [:p "Not found"]

       :else
       (if user
         (if (isa? scope ::scope/logout)
           (rf/dispatch [::event/push-state [::uri/feeds {:feeds "home"}]])
           wrapped-view)
         (if (isa? scope ::scope/login)
           (rf/dispatch [::event/push-state [::uri/welcome]])
           wrapped-view))))])
