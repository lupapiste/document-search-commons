(ns search-commons.utils.i18n
  (:require [search-commons.utils.state :as state]
            [search-commons.i18n :as i18n]))

(defn t [k]
  (i18n/t k (-> @state/translations :translations)))
