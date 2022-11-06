(ns kitsune.wrappers.format
  (:require [clojure.tools.logging :as log]
            [muuntaja.core :as m]
            [ring.util.http-response :refer [not-acceptable]]))

(def default-options
  (let [json-format (get-in m/default-options [:formats "application/json"])]
    (-> m/default-options
        (assoc :return :bytes)
        (update-in [:formats] select-keys ["application/json"])
        (assoc-in [:formats "application/activity+json"] json-format))))

(defn wrap-format
  [handler]
  (fn format-wrapper
    [{match :reitit.core/match
      suggested :muuntaja/response
      method :request-method
      :as request}]
    (if-let [produces (not-empty (get-in match
                                         [:result method :data :swagger :produces]
                                         (get-in match
                                                 [:result :data :swagger :produces])))]
      (if (= (:raw-format suggested) "*/*")
        (let [choice (or (produces (:format suggested))
                         (first produces))]
          (assoc (handler (assoc request :muuntaja/request choice))
                 :muuntaja/content-type choice))
        (if-let [common (produces (:raw-format suggested))]
          (assoc (handler (assoc request :muuntaja/request common))
                 :muuntaja/content-type common)
          (do
            (log/info "Not acceptable" (:raw-format suggested))
            (not-acceptable))))
      (handler request))))
