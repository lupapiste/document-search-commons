(ns search-commons.components.multi-select-view
  (:require [search-commons.utils.i18n :refer [t]]
            [search-commons.routing :as routing]
            [search-commons.utils.state :as state]
            [cognitect.transit :as transit]))

(def writer (transit/writer :json))

(defn select-view [& [mass-operations]]
  (let [total (count @state/multi-selected-results)]
    [:div.select-view
     [:div.select-view-header
      [:h4 (cond
             (= total 0) (t "0 dokumenttia valittu")
             (= total 1) (t "1 dokumentti valittu")
             (> total 1) (str total (t "dokumenttia valittu"))
             :else "Error")]]
     [:div.select-view-content.stacked
      [:button.secondary {:on-click state/multi-select-all-results
                          :disabled (> @state/total-result-count 200)}
       [:i.lupicon-file-check]
       (if (> @state/total-result-count 200)
         [:span (t "Kaikkia ei voi valita (max 200)")]
         [:span (t "Valitse kaikki tulokset")])]
      [:button.secondary {:on-click #(reset! state/multi-selected-results #{})
                          :disabled (= 0 total)}
       [:i.lupicon-remove]
       [:span (t "Poista valinnat")]]
      [:form {:id "multi-select-form" :action (routing/path "/mass-download") :method "POST"}
       [:input {:type "hidden" :name "docs" :value (transit/write writer @state/multi-selected-results)}]
       [:button.secondary {:type "submit" :disabled (= 0 total)}
        [:i.lupicon-download]
        [:span (t "Lataa valitut dokumentit")]]]
      [:div.undo-archive]]
     (when mass-operations [mass-operations])]))
