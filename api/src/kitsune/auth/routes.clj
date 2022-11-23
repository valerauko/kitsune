(ns kitsune.auth.routes
  (:require [kitsune.auth.session.handlers :as session]
            [kitsune.auth.session.store :refer [->CarmineStore]]
            [ring.middleware.session :refer [wrap-session]]))

(def routes
  ["/api/auth"
   ["/v1"
    ["/session"
     {:middleware [[wrap-session
                    {:store (->CarmineStore)
                     :cookie-name "kitsune_session"
                     :cookie-attrs {:same-site :strict
                                    :path "/api/auth/"
                                    :secure true
                                    :http-only true}}]]
      :post {:parameters {:form [:map
                                 [:email [:string {:min 5 :max 200}]]
                                 [:password [:string {:min 8 :max 1000}]]]}
             :name ::login
             :handler session/login}
      :put {:name ::refresh
            :handler session/refresh}
      :delete {:name ::logout
               :handler session/logout}}]]])
