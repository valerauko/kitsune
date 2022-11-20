(ns kitsune.routes
  (:require [re-frame.core :as rf]
            [reitit.coercion.malli]
            [reitit.frontend]
            [reitit.frontend.easy :as easy]
            [kitsune.auth.scopes :as scope]
            [kitsune.feeds.events :as feeds]
            [kitsune.routes.events :as events]
            [kitsune.uri :as uri]
            [kitsune.views.layouts.welcome :as layouts.welcome]
            [kitsune.views.pages.welcome :as pages.welcome]
            [kitsune.views.pages.timeline :as pages.timeline]))

(def routes
  ["/"
   {:coercion reitit.coercion.malli/coercion
    :scope ::scope/login}
   [""
    {:name ::uri/root
     :redirect-to [::uri/feeds {:feeds "home"}]}]
   ["welcome/"
    {:scope ::scope/logout
     :layout #'layouts.welcome/view}
    [""
     {:name ::uri/welcome
      :view #'pages.welcome/welcome}]
    ["login"
     {:name ::uri/login
      :view #'pages.welcome/login}]
    ["register"
     {:name ::uri/register
      :view #'pages.welcome/register}]]
   ["timeline"
    ["" {:redirect-to [::uri/feeds {:feeds "home"}]}]
    ["/"
     ["" {:redirect-to [::uri/feeds {:feeds "home"}]}]
     [":feeds"
      {:name ::uri/feeds
       :parameters {:path {:feeds string?}}
       :controllers [{:params #(-> % :parameters :path)
                      :start #(rf/dispatch [:kitsune.feeds.events/set-feeds
                                            (:feeds %)])}]
       :view #'pages.timeline/timeline}]]]])

(def router
  (reitit.frontend/router routes))

(extend-protocol uri/Link
  Keyword
  (uri/href
   ([route]
    (uri/href route nil nil))
   ([route params]
    (uri/href route params nil))
   ([route params query]
    (easy/href route params query))))

(defn on-navigate
  [new-match]
  (when new-match
    (rf/dispatch [::events/navigated new-match])))

(defn start-router
  "Used in the actual browser, using the browser's HTML5 History API"
  []
  (easy/start!
   router
   on-navigate
   {:use-fragment false}))
