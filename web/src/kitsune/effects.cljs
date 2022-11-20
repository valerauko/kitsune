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
