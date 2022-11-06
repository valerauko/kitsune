(ns kitsune.uri
  (:require [reitit.core :refer [match-by-name! match->path]]))

(def scheme
  "https://")

(def host
  "kitsune.loca.lt")

(defprotocol Url
  (url [_] [_ _]))

(defn with-domain
  [path]
  (str scheme host path))

(defn url-with-router
  [router route args]
  (if-let [match (match-by-name! router route args)]
    (-> match
        (match->path (select-keys args (some-> match :data :parameters :query keys)))
        (with-domain))
    (throw (ex-info (str "Unknown route: " route) {:route route :args args}))))
