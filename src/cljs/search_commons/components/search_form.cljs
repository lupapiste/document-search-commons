(ns search-commons.components.search-form
  (:require [cljs.pprint :refer [pprint]]
            [clojure.string :as string]
            [search-commons.domain :as domain]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]
            [search-commons.components.time :as time]
            [search-commons.components.combobox :as cb]
            [search-commons.components.map :as map]
            [lupapiste-commons.attachment-types :as attachment-types]
            [lupapiste-commons.usage-types :as usages]
            [reagent.ratom :refer [make-reaction]])
  (:require-macros [search-commons.utils.macros :refer [handler-fn]]))

(defn toggle-query-field
  ([field selected?]
    (toggle-query-field field selected? :fields))
  ([field selected? target-key]
    (let [op (if selected? conj disj)]
    (swap! state/search-query update target-key op field))))

(defn search-disabled? []
  (or (domain/search-disabled? (:text @state/search-query)
                               (:coordinates @state/search-query))
      (empty? (:targets @state/search-query))))

(defn input-form []
  [:div.search
   [:div.search-text
    [:form {:on-submit #(.preventDefault %)}
     [:input {:type "text"
             :placeholder (t "Kirjoita hakusana")
             :value (:text @state/search-query)
             :on-change #(state/update-search-field :text (.. % -target -value))
             :on-key-down #(when (and (= 13 (.. % -keyCode))
                                   (not (search-disabled?)))
                            (state/new-search))}]]
    [:button.search-button.secondary {:on-click state/reset-state}
     [:i.lupicon-remove]
     [:span (t "Tyhjennä")]]
    [:button.map-search.search-button.secondary {:on-click (handler-fn (swap! state/show-search-map not))}
     [:i {:class (if @state/show-search-map "lupicon-chevron-up" "lupicon-chevron-down")}]
     [:span (t "Kartta")]]
    [:button.search-button.primary {:disabled (search-disabled?)
                                    :on-click state/new-search}
     [:i.lupicon-search]
     [:span (t "Hae")]]
    [:a.help {:href (t "path.guide.document.search") :target "_blank" :title (t "help")}
     [:span.lupicon-circle-question]]
    (when (and @state/show-search-map (get-in @state/config [:config :cdn-host]))
      [map/ol-map {:data @state/results-for-map}])]
   [:div.search-options
    [:div.search-fields
     [:div.checkboxes
      [:label
       [:input {:type      "checkbox"
                :checked   (not (:tokenize? @state/search-query))
                :on-change #(swap! state/search-query assoc :tokenize? (not (.. % -target -checked)))}]
       [:span (t "Hae koko hakulausekkeella ilman tokenisointia")]]]
     [:h4 (t "Rajoita haku seuraaviin kenttiin")]
     [:div.checkboxes
      [:div.half
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :address)
                  :on-change #(toggle-query-field :address (.. % -target -checked))}]
         [:span (t :address)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :attachment.label.contents)
                  :on-change #(toggle-query-field :attachment.label.contents (.. % -target -checked))}]
         [:span (t :attachment.label.contents)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :applicant)
                  :on-change #(toggle-query-field :applicant (.. % -target -checked))}]
         [:span (t :applicant)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :tyomaasta-vastaava)
                  :on-change #(toggle-query-field :tyomaasta-vastaava (.. % -target -checked))}]
         [:span (t :tyomaasta-vastaava)]]]]
      [:div.half
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :designer)
                  :on-change #(toggle-query-field :designer (.. % -target -checked))}]
         [:span (t :designer)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :handler)
                  :on-change #(toggle-query-field :handler (.. % -target -checked))}]
         [:span (t :handler)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :propertyId)
                  :on-change #(toggle-query-field :propertyId (.. % -target -checked))}]
         [:span (t :propertyId)]]]
       [:div
        [:label
         [:input {:type      "checkbox"
                  :checked   (contains? (get @state/search-query :fields) :foreman)
                  :on-change #(toggle-query-field :foreman (.. % -target -checked))}]
         [:span (t :foreman)]]]]]
     (when (> (count (get-in @state/config [:user :organizations])) 1)
       [:div.organization-select.form-grid
        [:h4 (t "Hae vain valitun organisaation asiakirjoista")]
        [:div.select
         [:span.select-arrow.lupicon-chevron-small-down]
         [:select {:on-change #(state/update-search-field :organization (.. % -target -value))
                   :value (:organization @state/search-query)}
          [:option {:value ""} (t "Hae kaikista")]
          (map (fn [[org-id names]] ^{:key org-id} [:option {:value org-id} (:fi names)]) (get-in @state/config [:user :organizations]))]]])]
    [:div.search-filter
     [:h4 (t "Näytä vain")]
     [:div.filter-options

      [:div.filter-option
       [:label (t "Asiakirjatyyppi")]

       (let [type-group-and-docs (partition 2 attachment-types/Rakennusluvat-v2)
             type-map (->> (map (fn [[type-group docs]]
                                  (map (fn [type-id]
                                         (let [type-vec [type-group type-id]
                                               t-key (string/join "." (map name type-vec))]
                                           {(t t-key) type-vec})) docs)) type-group-and-docs)
                           (flatten)
                           (into {}))]
         [cb/combobox type-map false :type])]

      [time/timespan]

      (when (seq @state/operations)
        [:div.filter-option
         [:label (t "Toimenpide")]
         [cb/combobox (into {} (map (fn [oper] {(t oper) oper}) @state/operations)) false :operation]])

      [time/closed-timespan]

      (when (some #(= % "R") (get-in @state/config [:user :permit-types]))
        [:div.filter-option
         [:label (t "Käyttötarkoitus")]

         (let [usage-map (into {} (map (fn [usage]
                                         (let [name (:name usage)]
                                           {(t name) name})) usages/rakennuksen-kayttotarkoitus))]
           [cb/combobox usage-map true :usage])])
      ]
     (when (and (get-in @state/config [:config :onkalo-enabled?])
                (get-in @state/config [:config :lupapiste-enabled?]))
       [:div.targets
        [:h4 (t "Haun kohdejärjestelmät")]
        [:div
         [:div.half
          [:label
           [:input {:type      "checkbox"
                    :checked   (contains? (get @state/search-query :targets) :lupapiste)
                    :on-change #(toggle-query-field :lupapiste (.. % -target -checked) :targets)}]
           [:span (t "Asiointi")]]]
         [:div.half
          [:label
           [:input {:type      "checkbox"
                    :checked   (contains? (get @state/search-query :targets) :onkalo)
                    :on-change #(toggle-query-field :onkalo (.. % -target -checked) :targets)}]
           [:span (t "Säilytysjärjestelmä")]]]]])]]])
