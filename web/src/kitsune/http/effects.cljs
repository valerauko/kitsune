(ns kitsune.http.effects
  (:require [re-frame.core :as rf]))

(rf/reg-fx
 ::http
 (fn [{:keys [path on-success on-error opts]
       :or {opts {}}}]
   (-> (js/fetch
        (str "/api" path)
        (clj->js (merge {:redirect :error
                         :signal (js/AbortSignal.timeout 500)
                         :cache :reload
                         :credentials :omit}
                        opts)))
       (.then (fn [response]
                (if (<= 200 (.-status response) 299)
                  (.then
                   (.json response)
                   #(rf/dispatch (conj on-success %)))
                  (rf/dispatch (conj on-error response)))))
       (.catch #(rf/dispatch (conj on-error %))))))
