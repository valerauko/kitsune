(ns kitsune.lang)

(defmacro ... [& input]
  (loop [aggr {}
         items input]
    (if (empty? items)
      aggr
      (let [head (first items)
            tail (rest items)]
        (if (symbol? head)
          (recur (assoc aggr (keyword head) head) tail)
          (recur (assoc aggr head (first tail)) (rest tail)))))))
