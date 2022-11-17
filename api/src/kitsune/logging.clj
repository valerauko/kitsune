(ns kitsune.logging
  (:require [mount.core :refer [defstate]]
            [com.brunobonacci.mulog :as u]
            [com.brunobonacci.mulog.buffer :as rb])
  (:import [java.sql
            Timestamp]
           [com.brunobonacci.mulog.publisher
            PPublisher]))

(def level
  :debug)

(def levels
  (let [level (some-> level (keyword) (or :debug))]
    (loop [lvls '(:trace :debug :info :warn :error :fatal)]
      (let [check (first lvls)]
        (if (= check level)
          lvls
          (recur (rest lvls)))))))

(intern *ns* 'with-context `u/with-context)

(defmulti format-line :mulog/event-name)

(defmethod format-line :default
  [line]
  (pr-str line))

(defn format-event
  [{::keys [level]
    :mulog/keys [timestamp root-trace trace-id]
    :as line}]
  (format "%tY/%1$tm/%1$td %1$tT.%1$tL [%s] [%s] %s"
          timestamp (or root-trace trace-id)
          (-> level (or :debug) name (.toUpperCase))
          (format-line line)))

(defn log-xform
  [events]
  (->> events
       (filter #(some #{(or (::level %) :debug)} levels))
       (map format-event)))

(defn publisher
  [{xform :transform}]
  (let [buffer (rb/agent-buffer 10000)]
    (reify PPublisher
      (agent-buffer
       [_]
       buffer)

      (publish-delay
       [_]
       50)

      (publish
       [_ buffer]
       (doseq [item (->> (rb/items buffer) (map second) xform)]
         (println item))
       (rb/clear buffer)))))

(defstate logger
  :start
  (u/start-publisher! ; {:type :console}
                      {:type :custom
                       :fqn-function "kitsune.logging/publisher"
                       :transform log-xform})
  :stop
  (logger))

(defmacro trace
  [& args]
  `(u/log ~@args ::level :trace))

(defmacro debug
  [& args]
  `(u/log ~@args ::level :debug))

(defmacro info
  [& args]
  `(u/log ~@args ::level :info))

(defmacro warn
  [& args]
  `(u/log ~@args ::level :warn))

(defmacro error
  [& args]
  `(u/log ~@args ::level :error))

(defmacro fatal
  [& args]
  `(u/log ~@args ::level :fatal))
