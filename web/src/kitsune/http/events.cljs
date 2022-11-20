(ns kitsune.http.events
  (:require [re-frame.core :as rf]
            [kitsune.http.effects :as fx]))

(rf/reg-event-fx
 ::load
 (fn [{{{:keys [in-flight]} ::state :as db} :db :or {in-flight 0}}
      [_ {:keys [coords on-final on-success on-error opts path]}]]
   (let [state (get-in db (cons ::state coords) {:state :new})]
     ;; don't fire more than one request for the same coords
     (when-not (= (:state state) :loading)
       (let [callback {:coords coords
                       :loaded on-final
                       :on-success on-success
                       :on-error on-error}]
         {:db (assoc-in db [::state :in-flight] (inc in-flight))
          ::fx/http {:path path
                     :opts opts
                     :on-success [::http-success callback]
                     :on-error [::http-error callback]}})))))

(rf/reg-event-fx
 ::http-success
 (fn [{db :db} [_ {:keys [on-success coords] :as opts} response]]
   {:db (assoc-in db (cons ::state coords) {:state :ready})
    :dispatch-n (let [dispatch [[::http-finish opts]]]
                  (if on-success
                    (conj dispatch [on-success response])
                    dispatch))}))

(rf/reg-event-fx
 ::http-error
 (fn [{db :db} [_ {:keys [on-error coords] :as opts} response]]
   {:db (assoc-in db (cons ::state coords) {:state :error})
    :dispatch-n (let [dispatch [[::http-finish opts]]]
                  (if on-error
                    (conj dispatch [on-error response])
                    dispatch))}))

(rf/reg-event-fx
 ::http-finish
 (fn [{{{:keys [in-flight]} ::state :as db} :db :or {in-flight 1}}
      [_ {:keys [coords on-final]}]]
   (let [new-in-flight (dec in-flight)
         result {:db (assoc-in db [::state :in-flight] new-in-flight)}]
     (if (and on-final (< new-in-flight 1))
       (assoc result :dispatch [on-final coords])
       result))))
