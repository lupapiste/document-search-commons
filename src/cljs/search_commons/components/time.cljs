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

(defn date-selector [{:keys [date-atom css-class read-only read-only-css-class disabled]}]
  (with-meta
    (fn []
      (let [input-class (when read-only (or read-only-css-class "read-only-date-field"))]
        [pikaday/date-selector {:date-atom date-atom
                                :class css-class
                                :pikaday-attrs {:format "DD.MM.YYYY"
                                                :i18n (pikaday-i18n)}
                                :input-attrs   {:read-only read-only
                                                :class input-class
                                                :disabled disabled}}]))
    {:component-did-mount (fn [this]
                            (let [node (reagent/dom-node this)]
                              (.addEventListener node "input" (fn [] (when (= "" (.-value node))
                                                                        (reset! date-atom nil))))))}))

(defn timespan []
  (let [react-key (str "pikaday-" (-> @state/translations :current-lang name))]
    [:div.filter-option.timespan.date-wrapper
     [:label (t "Päätös annettu aikavälillä")]
     ^{:key (str "pikaday-start-" react-key)} [(date-selector {:date-atom state/start-date :css-class "start-date"})]
     [:span.date-separator "–"]
     ^{:key (str "pikaday-end-" react-key)} [(date-selector {:date-atom state/end-date :css-class "end-date"})]]))

(defn closed-timespan []
  (let [react-key (str "pikaday-" (-> @state/translations :current-lang name))]
    [:div.filter-option.timespan.date-wrapper
     [:label (t "Hanke valmistunut aikavälillä")]
     ^{:key (str "pikaday-start-" react-key)} [(date-selector {:date-atom state/closed-start-date :css-class "start-date"})]
     [:span.date-separator "–"]
     ^{:key (str "pikaday-end-" react-key)} [(date-selector {:date-atom state/closed-end-date :css-class "end-date"})]]))

(defn date-field [{:keys [label value-atom disabled tooltip visible? prevent-manual-entry on-click]}]
  (when (or visible? (nil? visible?))
    [:div.date-wrapper {:title tooltip :on-click on-click}
     [:label label]
     [(date-selector {:date-atom value-atom
                      :read-only prevent-manual-entry
                      :disabled disabled})]]))
