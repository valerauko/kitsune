(ns kitsune.fed.conversions
  (:require [clojure.string :as str]
            [kitsune.lang :refer [...]])
  (:import [java.net
            URI]))

(defn strict-trimmed
  [str]
  (-> str
      ;; trim from head then reverse to avoid regex dos
      (str/replace #"^\W+" "")
      (str/reverse)
      (str/replace #"^\W+" "")
      (str/reverse)))

(defn person->account
  [{:keys [id inbox]
    display-name :name
    name :preferredUsername
    {public-key :publicKeyPem} :publicKey
    {shared-inbox :sharedInbox} :endpoints}]
  (let [domain (.getHost (URI. id))]
    (... :uri id :name (strict-trimmed name)
         domain inbox shared-inbox public-key display-name)))
