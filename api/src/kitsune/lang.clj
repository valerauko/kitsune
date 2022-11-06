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

(defn qualify-sym
  "From https://stackoverflow.com/a/71585112. No idea why there is no built-in."
  [s]
  (if (simple-symbol? s)
    (or (some-> (ns-refers *ns*) (get s) symbol)
        (symbol (str *ns*) (str s)))
    (let [nsp (namespace s)
          n (name s)
          aliases (ns-aliases *ns*)]
      (symbol (or (some-> aliases (get (symbol nsp)) ns-name str) nsp) n))))
