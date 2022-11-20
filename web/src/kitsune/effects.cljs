(ns kitsune.effects
  (:require [re-frame.core :as rf]
            [cognitect.transit :as t]))

(def local-storage-key
  (str "kitsune@" js/window.location.hostname))

(def persist
  (rf/after
   (fn [{data :data}]
     (let [encoder (t/writer :json)]
       (->> data
            (t/write encoder)
            (js/localStorage.setItem local-storage-key))))))

(rf/reg-cofx
 ::persisted
 (fn [state _]
   (let [decoder (t/reader :json)
         data (some->> local-storage-key
                       (js/localStorage.getItem)
                       (t/read decoder))]
     (assoc state :persisted data))))

(rf/reg-fx
 ::http
 (fn [{:keys [path method on-success on-error opts]
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
