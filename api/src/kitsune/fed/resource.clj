(ns kitsune.fed.resource
  (:require [kitsune.cache :as cache]
            [kitsune.fed.http :as http]
            [kitsune.logging :as log]))

(defmulti -find-resource
  (fn -find-resource-dispatch [type _]
    type))

(defmulti -store-resource
  (fn -store-resource-dispatch [object]
    (some->> object (:type) (str) (.toLowerCase) (keyword (str *ns*)))))

(defmethod -find-resource :default
  -find-unknown
  [type id]
  (log/debug ::unknown ::type type ::id id))

(defmethod -store-resource :default
  -store-unknown
  [thing]
  (log/debug ::unknown ::object thing))

(defn store
  [{:keys [id] :as object}]
  (some->> object
           (-store-resource)
           (into {})
           (cache/set id)))

(defn refetch
  [uri]
  (let [{:keys [status body]} @(http/fetch-resource uri)]
    (when (and status (<= 200 status 299))
      (some->> (http/parse-json body)
               (store)))))

(defn find-resource
  [type uri]
  (or (cache/get uri)
      (some->> uri
               (-find-resource type)
               (into {})
               (cache/set uri))))

(defn find-or-fetch
  [type uri]
  (or (find-resource type uri)
      (refetch uri)))
