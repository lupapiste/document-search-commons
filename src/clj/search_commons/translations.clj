(ns search-commons.translations
  (:require [clojure.java.io :as io]
            [com.stuartsierra.component :as component]
            [lupapiste-commons.i18n.core :refer [merge-translations read-translations keys-by-language]]
            [taoensso.timbre :as timbre]))

(defn load-translations []
  (keys-by-language (merge-translations (read-translations (io/resource "shared_translations.txt"))
                                        (read-translations (io/resource "translations.txt")))))

(defrecord Translations []
  component/Lifecycle
  (start [this]
    (timbre/info "Loading translations")
    (assoc this :data (load-translations)))
  (stop [this]
    (assoc this :data nil)))
