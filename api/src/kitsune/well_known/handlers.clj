(ns kitsune.well-known.handlers
  (:require [ring.util.http-response :refer [ok not-found]]
            [jsonista.core :as json]
            [next.jdbc :as jdbc]
            [kitsune.uri :as uri :refer [url]]
            [kitsune.db.account :refer [find-local-by-name]]
            [kitsune.fed.routes :as-alias fed]))

(defn host-meta
  [{method :method :as req}]
  (ok
   [:XRD
    {:xmlns "http://docs.oasis-open.org/ns/xri/xrd-1.0"}
    [:Link
     {:rel "lrdd"
      :type "application/xrd+xml"
      :template (str (url :kitsune.well-known.routes/webfinger) "?resource={uri}")}]]))

(defn node-info
  [{{{:keys [version]} :path} :parameters}]
  {:status 200
   :headers
   {"Content-Type" "application/json; profile=\"http://nodeinfo.diaspora.software/ns/schema/2.1#\"; charset=utf-8"}
   :body
   (json/write-value-as-bytes
    {:version version
     :software {:name "Kitsune"
                :version "0.3.0"
                :homepage "https://kitsune.social"
                :repository "https://github.com/valerauko/kitsune"}
     :metadata {:nodeName "Kitsune dev server"}
     :protocols ["activitypub"]
     :openRegistrations false
     :usage {:localPosts 0
             :users {:total 1
                     :activeHalfyear 1
                     :activeMonth 1}}
     :services {:inbound []
                :outbound []}})})

(defn node-schema
  [_]
  (ok
   {:links [{:rel "http://nodeinfo.diaspora.software/ns/schema/2.0"
             :href "https://kitsune.loca.lt/.well-known/nodeinfo/2.0"}
            {:rel "http://nodeinfo.diaspora.software/ns/schema/2.1"
             :href "https://kitsune.loca.lt/.well-known/nodeinfo/2.1"}]}))

(defn webfinger
  [{fmt :muuntaja/request
    {{:keys [resource]} :query} :parameters}]
  (if-let [[user host] (->> resource
                            (re-find #"(?i)(?:acct:)?@?([^@]+)(?:@([^\pZ]+))?")
                            (rest)
                            (not-empty))]
    (if (or (empty? host) (= host uri/host))
      (if-let [{:accounts/keys [acct name uri]} (find-local-by-name user)]
        (let [subject (str "acct:" acct)
              links [{:href (url :kitsune.routes/profile {:name name})
                      :rel "http://webfinger.net/rel/profile-page"
                      :type "text/html"}
                     {:href uri
                      :rel "self"
                      :type "application/activity+json"}
                     {:rel "http://ostatus.org/schema/1.0/subscribe"
                      ;; reitit's match-to-path in `url` escapes the {} so str
                      :template (str (url ::fed/authorize-follow) "?acct={uri}")}]]
          (ok
           (case fmt
             ("application/xml" "application/xrd+xml")
             (reduce
              conj
              [:XRD
               {:xmlns "http://docs.oasis-open.org/ns/xri/xrd-1.0"}
               [:Subject subject]]
              (map (fn [attrs] [:Link attrs]) links))
             ;; else
             {:subject subject
              :links links})))
        (not-found))
      (not-found))))
