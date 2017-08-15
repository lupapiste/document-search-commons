(ns search-commons.components.multi-select-view
  (:require [search-commons.utils.i18n :refer [t]]
           [search-commons.routing :as routing]
           [search-commons.utils.state :as state]))

(defn select-view []
  [:div.select-view
   [:div.select-view-header
    (let [total (count @state/multi-selected-results)]
      [:h4 (str total " " (if (= 1 total) (t "dokumentti") (t "dokumenttia")) " valittu")])]
   [:div.select-view-content.stacked
    [:button.secondary {:on-click state/multi-select-all-results}
     [:i.lupicon-checkbox-on]
     [:span (t "Valitse kaikki tulokset")]]
    [:button.secondary {:on-click #(reset! state/multi-selected-results #{})}
     [:i.lupicon-checkbox-off]
     [:span (t "Poista valinnat")]]
    [:form {:id "multi-select-form" :action (routing/path "/mass-download") :method "POST"}
     [:input {:type "hidden" :name "docs" :value (js/encodeURIComponent (pr-str @state/multi-selected-results))}]
     [:button.secondary {:type "submit"}
      [:i.lupicon-download]
      [:span (t "Lataa valitut dokumentit")]]]]])

