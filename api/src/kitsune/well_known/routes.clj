(ns kitsune.well-known.routes
  (:require [muuntaja.core :as muuntaja]
            [kitsune.format.xml :as xml]
            [kitsune.wrappers.format :as fmt]
            [kitsune.well-known.handlers :refer [host-meta node-info node-schema webfinger]]))

(def format-options
  (let [json-format (get-in fmt/default-options [:formats "application/json"])]
    (-> fmt/default-options
        (assoc-in [:formats "application/jrd+json"] json-format)
        (assoc-in [:formats "application/xml"] xml/format)
        (assoc-in [:formats "application/xrd+xml"] xml/format))))

(def routes
  ["/.well-known"
   {:muuntaja (muuntaja/create format-options)}
   ["/host-meta"
    {:get {:summary "Host metadata"
           :swagger {:produces #{"application/xrd+xml" "application/xml"}
                     :tags ["WebFinger"]}
           :handler host-meta}}]
   ["/nodeinfo"
    [""
     {:swagger {:produces #{"application/jrd+json" "application/json"}}
      :get {:handler node-schema}}]
    ["/:version"
     {:swagger {:produces #{"application/json; profile=\"http://nodeinfo.diaspora.software/ns/schema/2.1#\"; charset=utf-8"
                            "application/json"}}
      :parameters {:path {:version [:enum "2.0" "2.1"]}}
      :get {:handler node-info}}]]
   ["/webfinger"
    {:name ::webfinger
     :get {:summary "WebFinger endpoint for users"
           :swagger {:produces #{"application/xrd+xml" "application/xml"
                                 "application/jrd+json" "application/json"}
                     :tags ["WebFinger"]}
           :parameters {:query {:resource :string}}
           :handler webfinger}}]])
