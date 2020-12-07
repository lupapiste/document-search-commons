(ns search-commons.utils
  (:require [clojure.string :as s]
            [clojure.pprint :refer [cl-format]]
            [search-commons.shared-utils :as shared-utils]))

(def property-id-regex shared-utils/property-id-regex)

(def zero-padded-property-id shared-utils/zero-padded-property-id)

(defn property-id-regexp-string [tokens]
  (when-let [regexes (->> tokens
                          (filter #(re-matches #"^\d+-*\d*-*\d*-*\d*" %))
                          (map (fn [term] (str (s/replace term "-" "0*") "[0-9]*")))
                          (seq))]
    (s/join "|" regexes)))
