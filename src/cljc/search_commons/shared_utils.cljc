(ns search-commons.shared-utils
  (:require [clojure.string :as s]
            [clojure.pprint :refer [cl-format]]))

(def db-property-id-regex #"^\d{14}$")
(def property-id-regex #"(\d{1,3})-(\d{1,3})-(\d{1,4})-(\d{1,4})")

;; Common Lisp format string: https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node200.html
(def lpad-3 "~3,1,0,'0@A")
(def lpad-4 "~4,1,0,'0@A")
;;           || | |  |||print as string
;;           || | |  ||pad left
;;           || | |  |padding character
;;           || | |minimum padding, 0 means dont' pad if not necessary
;;           || |insert one padding character at a time
;;           ||pad at least this amount
;;           |start a directive, like %

(defn zero-padded-property-id [^String value]
  (when-let [[_ & parts] (re-matches property-id-regex value)]
    (let [format-string (apply str [lpad-3 lpad-3 lpad-4 lpad-4])]
      (apply cl-format nil format-string parts))))

(defn- human-readable-property-id? [property-id]
  (re-matches property-id-regex (or property-id "")))

(defn- db-property-id? [property-id]
  (re-matches db-property-id-regex (or property-id "")))

(defn property-id? [property-id]
  (boolean (or (human-readable-property-id? property-id)
               (db-property-id? property-id))))

(defn ->db-property-id [property-id]
  (cond
    (human-readable-property-id? property-id) (zero-padded-property-id property-id)
    (db-property-id? property-id) property-id
    :else nil))

(def rakennustunnus-pattern
  "VRK pysyva rakennustunnus. KRYSP-skeemassa: ([1][0-9]{8})[0-9ABCDEFHJKLMNPRSTUVWXY]
   Modified version from sade.validators"
  #?(:clj (re-pattern "^1\\d{8}[0-9A-FHJ-NPR-Y]$")
     :cljs #"^[1][0-9]{8}[0-9A-FHJ-NPR-Y]$"))

(defn- matches? [re s] (boolean (when (string? s) (re-matches re s))))

(defn rakennustunnus? [prt]
  (and (matches? rakennustunnus-pattern prt)))

(defn ->tokens
  "Split string by comma or whitespace"
  [^String s]
  (if s
    (remove empty? (s/split s #"\s*,\s*|\s+"))
    []))
