(ns search-commons.utils
  (:require [clojure.string :as s]
            [clojure.pprint :refer [cl-format]]))

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

(defn property-id-regexp-string [tokens]
  (when-let [regexes (->> tokens
                          (filter #(re-matches #"^\d+-*\d*-*\d*-*\d*" %))
                          (map (fn [term] (str (s/replace term "-" "0*") "[0-9]*")))
                          (seq))]
    (s/join "|" regexes)))
