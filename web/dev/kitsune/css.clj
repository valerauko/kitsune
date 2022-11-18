(ns kitsune.css
  (:require [clojure.java.io :as io]
            [shadow.css.build :as cb]
            [shadow.cljs.devtools.server.fs-watch :as fs-watch]))

(defonce css-ref (atom nil))
(defonce css-watch-ref (atom nil))

(defn generate-css []
  (let [result
        (-> @css-ref
            (cb/generate '{:ui {:include [kitsune*]}})
            (cb/write-outputs-to (io/file "public" "css")))]
    (binding [*out* (io/writer (System/out))]
      (prn [:CSS] :generated)
      (doseq [mod (:outputs result)
              {:keys [warning-type] :as warning} (:warnings mod)]
        (prn [:CSS] (name warning-type) (dissoc warning :warning-type)))
      (println))))

(defn watch
  {:shadow/requires-server true}
  []

  ;; first initialize my css
  (reset! css-ref
          (-> (cb/start)
              (cb/index-path (io/file "src" "kitsune") {})))

  ;; then build it once
  (generate-css)

  ;; then setup the watcher that rebuilds everything on change
  (reset! css-watch-ref
          (fs-watch/start
           {}
           [(io/file "src" "kitsune")]
           ["cljs"]
           (fn [updates]
             (try
               (doseq [{:keys [file event]} updates
                       :when (not= event :del)]
                 ;; re-index all added or modified files
                 (swap! css-ref cb/index-file file))

               (generate-css)
               (catch Throwable e
                 (binding [*out* (io/writer (System/out))]
                   (prn [:CSS] :build-failure e)))))))

  ::started)

(defn stop []
  (when-some [css-watch @css-watch-ref]
    (fs-watch/stop css-watch)
    (reset! css-ref nil))

  ::stopped)

(defn build []
  (reset! css-ref
          (-> (cb/start)
              (cb/index-path (io/file "src" "kitsune") {})))

  ;; then build it once
  (generate-css))
