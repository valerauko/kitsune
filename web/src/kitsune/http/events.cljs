(ns kitsune.http.events
  (:require [cljs.reader :as reader]
            [re-frame.core :as rf]
            [kitsune.effects :as fx]))

(rf/reg-event-fx
 ::load
 (fn [{{{:keys [in-flight]} ::state :as db} :db :or {in-flight 0}}
      [_ {:keys [path coords loaded opts]}]]
   (let [state (get-in db (cons ::state coords) {:state :new})]
     (when-not (= (:state state) :loading)
       (let [callback {:coords coords
                       :loaded loaded}]
         {:db (assoc-in db [::state :in-flight] (inc in-flight))
          ::fx/http {:path path
                     :opts opts
                     :on-success [::http-success callback]
                     :on-error [::http-error callback]}})))))

(defn timestamps
  [{:keys [created-at updated-at] :as obj}]
  (-> obj
      (assoc :created-at (reader/parse-timestamp created-at))
      (assoc :updated-at (reader/parse-timestamp updated-at))))

(defn convert
  [body]
  (let [parsed (js->clj body :keywordize-keys true)]
    ; (cond
    ;   (vector? parsed) (mapv timestamps parsed)
    ;   (map? parsed) (timestamps parsed)
    ;   :else parsed))
    parsed))

(rf/reg-event-fx
 ::http-success
 (fn [{db :db} [_ {:keys [coords] :as opts} body]]
   {:db (-> db
            (assoc-in (cons ::state coords)
                      {:state :ready})
            (assoc-in (cons :data coords)
                      (convert body)))
    :dispatch [::http-finish opts]}))

(rf/reg-event-fx
 ::http-error
 (fn [{db :db} [_ {:keys [coords] :as opts} _response]]
   {:db (-> db
            (assoc-in (cons ::state coords)
                      {:state :error})
            (assoc-in (cons :data coords)
                      nil))
    :dispatch [::http-finish opts]}))

(rf/reg-event-fx
 ::http-finish
 (fn [{{{:keys [in-flight]} ::state :as db} :db :or {in-flight 1}}
      [_ {:keys [coords loaded]}]]
   (let [new-in-flight (dec in-flight)
         result {:db (assoc-in db [::state :in-flight] new-in-flight)}]
     (if (and loaded (< new-in-flight 1))
       (assoc result :dispatch [loaded coords])
       result))))
