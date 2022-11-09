(ns kitsune.fed.inbox.delete
  (:require [clojure.tools.logging :as log]
            [kitsune.fed.http :as http]
            [kitsune.db.account :as account]
            [kitsune.db.note :as note]))

(defn deref-type
  [uri]
  (let [{:keys [body error]} @(http/deref-uri uri)]
    (when-not error
      (not-empty (:type (http/parse-json body))))))

(defn delete-known
  [object-type object-id]
  (if (empty? object-id)
    (log/info "Don't know how to delete unidentifiable object")
    (case object-type
      "Person"
      (when (account/delete-by-uri object-id)
        (log/info "Deleted account" object-id))

      "Note"
      (when (note/delete-by-uri object-id)
        (log/info "Deleted note" object-id))

      ;; else (unhandled type)
      (log/info (str "Don't know how to delete '" object-type "' " object-id)))))

(defn delete-unknown
  [object-id]
  (cond
    (empty? object-id)
    (log/info "Don't know how to delete unidentifiable object")

    (note/delete-by-uri object-id)
    (log/info "Deleted note" object-id)

    (account/delete-by-uri object-id)
    (log/info "Deleted account" object-id)

    :else
    (log/info (str "Don't know how to delete unknown type " object-id))))

(defn delete
  [{:keys [actor object] :as activity}]
  (cond
    ;; if the object is just a string, the type is unknown
    (string? object)
    (if (= actor object)
      ;; the user was deleted, time to wipe
      ;; not necessarily Person, but actor-type
      (delete-known "Person" object)
      ;; type is unknown so first try to deref,
      ;; maybe there is a helpful Tombstone
      (if-let [object-type (deref-type object)]
        (delete-known object-type object)
        (delete-unknown object)))

    ;; if it's a Tombstone
    (= (:type object) "Tombstone")
    (if-let [former-type (:formerType object)]
      ;; if the former type is known
      (delete-known former-type (:id object))
      ;; if the type is unknown
      (delete-unknown (:id object)))

    ;; if it's any other kind of object
    (map? object)
    (if-let [object-type (:type object)]
      ;; type is known
      (delete-known object-type (:id object))
      ;; type is still unknown how unhelpful
      (if-let [id (:id object)]
        (delete-unknown id)
        ;; there's not even an id, give up
        (log/info "Don't know how to delete unidentifiable object" object)))

    :else
    ;; some weird data-shape, probably a vector of things
    (log/info "Don't know how to delete unidentifiable object" object)))
