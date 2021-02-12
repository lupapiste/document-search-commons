(ns search-commons.utils.time
  (:import [goog.i18n DateTimeFormat]))

(defn format-date [iso-date-string]
  (when iso-date-string
    (.format (DateTimeFormat. "d.M.yyyy") (js/Date. iso-date-string))))
