(ns search-commons.domain
  (:require [clojure.string :as s]))

(defn search-disabled? [text coordinates]
  (and (s/blank? text) (empty? coordinates)))
