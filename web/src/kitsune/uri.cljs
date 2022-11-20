(ns kitsune.uri)

(defprotocol Link
  (href [_] [_ _] [_ _ _]))
