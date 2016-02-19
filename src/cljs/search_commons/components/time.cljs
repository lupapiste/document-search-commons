(ns search-commons.components.time
  (:require [search-commons.utils.state :as state]
            [search-commons.utils.i18n :refer [t]]
            [cljs-pikaday.reagent :as pikaday]
            [cljsjs.moment]
            [reagent.core :as reagent]))

(defn pikaday-i18n []
  {:previous-month (t "Edellinen kuukausi")
   :next-month (t "Seuraava kuukausi"),
   :months [(t "Tammikuu") (t "Helmikuu") (t "Maaliskuu") (t "Huhtikuu") (t "Toukokuu") (t "Kesäkuu") (t "Heinäkuu") (t "Elokuu") (t "Syyskuu") (t "Lokakuu") (t "Marraskuu") (t "Joulukuu")]
   :weekdays [(t "Sunnuntai") (t "Maanantai") (t "Tiistai") (t "Keskiviikko") (t "Torstai") (t "Perjantai") (t "Lauantai")]
   :weekdays-short [(t "Su") (t "Ma") (t "Ti") (t "Ke") (t "To") (t "Pe") (t "La")]})

(defn date-selector [date-atom css-class]
  (with-meta
    (fn []
      [pikaday/date-selector {:date-atom date-atom
                              :class css-class
                              :pikaday-attrs {:format "DD.MM.YYYY"
                                              :i18n (pikaday-i18n)}}])
    {:component-did-mount (fn [this]
                            (let [node (reagent/dom-node this)]
                              (.addEventListener node "input" (fn [] (when (= "" (.-value node))
                                                                        (reset! date-atom nil))))))}))

(defn timespan []
  (let [react-key (str "pikaday-" (-> @state/translations :current-lang name))]
    [:div.filter-option.timespan
     [:label (t "Päätös annettu aikavälillä")]
     ^{:key (str "pikaday-start-" react-key)} [(date-selector state/start-date "start-date")]
     [:span.date-separator "–"]
     ^{:key (str "pikaday-end-" react-key)} [(date-selector state/end-date "end-date")]]))
