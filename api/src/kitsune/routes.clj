(ns kitsune.routes
  (:require [muuntaja.core :as muuntaja]
            [reitit.coercion.malli :as coercion]
            [reitit.core :refer [match-by-path]]
            [reitit.ring]
            [reitit.ring.coercion :refer [coerce-exceptions-middleware
                                          coerce-request-middleware
                                          coerce-response-middleware]]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.ring.middleware.muuntaja :refer [format-middleware]]
            [reitit.swagger :refer [create-swagger-handler]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [kitsune.fed.routes :as fed]
            [kitsune.uri :as uri]
            [kitsune.well-known.routes :as well-known]
            [kitsune.wrappers.format :as fmt :refer [wrap-format]]
            [kitsune.wrappers.logging :refer [wrap-logging]])
  (:import [clojure.lang
            Keyword]))

(def router
  (reitit.ring/router
   [["/@:name"
     {:name ::profile
      :parameters {:path {:name :string}}}]
    fed/routes
    well-known/routes
    ["/openapi.json"
     {:get {:no-doc true
            :swagger {:info {:title "Kitsune API"
                             :description "Very fox microblogging"
                             :version "0.3.0"}
                      :basePath "/"}
            :handler (create-swagger-handler)}}]]
   {:data
    {:muuntaja (muuntaja/create fmt/default-options)
     :coercion (coercion/create {})
     :middleware
     [parameters-middleware
      format-middleware
      wrap-format
      coerce-exceptions-middleware
      coerce-response-middleware
      coerce-request-middleware]}}))

;; close over router to avoid circular dependency in handlers
(extend-protocol uri/Url
  Keyword
  (uri/url
   ([kw] (uri/url kw {}))
   ([kw args]
    (uri/url-with-router router kw args))))

(extend-protocol uri/Match
  String
  (uri/match
   ([path]
    (match-by-path router path))))

(def handler
  (-> router
      (reitit.ring/ring-handler
       (reitit.ring/create-default-handler)
       {:inject-router false})
      wrap-logging))
