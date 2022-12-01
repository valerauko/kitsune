(ns kitsune.format.xml
  (:refer-clojure :exclude [format])
  (:require [muuntaja.format.core :as m]
            [clojure.data.xml :as xml]
            [clojure.java.io :as io])
  (:import [java.io
            ByteArrayOutputStream
            OutputStream]))

(defn decoder [options]
  (reify
    m/Decode
    (decode [_ data _]
      (xml/parse data))))

(defn encoder [_]
  (reify
    m/EncodeToBytes
    (encode-to-bytes [_ data charset]
      (-> data
          (xml/sexp-as-element)
          (xml/emit-str :encoding charset)
          (.getBytes)))

    m/EncodeToOutputStream
    (encode-to-output-stream [_ data charset]
      (fn [^OutputStream out-stream]
        (let [writer (io/writer out-stream)]
          (xml/emit (xml/sexp-as-element data) writer :encoding charset)
          (.flush out-stream))))))

(def format
  (m/map->Format
   {:name "application/xml"
    :decoder [decoder]
    :encoder [encoder]}))
