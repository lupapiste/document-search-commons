(ns search-commons.components.search-form
  (:require [cljs.pprint :refer [pprint]]
            [clojure.string :as string]
            [search-commons.domain :as domain]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]
            [search-commons.components.time :as time]
            [search-commons.components.combobox :as cb]
            [search-commons.components.map :as map]
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

(defn field-limit-checkbox [property-kw]
  [:div {:key (str (name property-kw) "-prop-checkbox")}
   [:label
    [:input {:type      "checkbox"
             :checked   (contains? (get @state/search-query :fields) property-kw)
             :on-change #(toggle-query-field property-kw (.. % -target -checked))}]
    [:span (t property-kw)]]])

(defn generate-available-name [name-map text suffix]
  (let [key (str text " (" suffix ")")]
    (if (contains? name-map key)
      (generate-available-name name-map text (inc suffix))
      key)))

(defn type-map [available-attachment-types]
  (->> available-attachment-types
       (reduce (fn [acc type-vec]
                 (let [text (->> (map name type-vec) (string/join ".") t)
                       key (if (contains? acc text)
                             (generate-available-name acc text 2)
                             text)]
                   (assoc acc key type-vec)))
               {})))

(defn attachment-type-filter []
  [:div.filter-option
   [:label (t "Asiakirjatyyppi")]
   [cb/combobox (type-map @state/available-attachment-types) false :type]])

(defn input-form [& [onkalo-deleted-query]]
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
       (doall
         (map field-limit-checkbox [:address :attachment.label.contents :applicant :tyomaasta-vastaava :projectDescription]))]
      [:div.half
       (doall
         (map field-limit-checkbox [:designer :handler :propertyId :foreman]))]]
     (when (seq (rest (get-in @state/config [:user :organizations])))
       [:div.organization-select.form-grid
        [:h4 (t "Hae vain valitun organisaation asiakirjoista")]
        [:div.select
         [:span.select-arrow.lupicon-chevron-small-down]
         [:select {:on-change #(state/update-search-field :organization (.. % -target -value))
                   :value (:organization @state/search-query)}
          [:option {:value ""} (t "Hae kaikista")]
          (->> (get-in @state/config [:user :organizations])
               (map (fn [[org-id {:keys [name]}]]
                      ^{:key org-id} [:option {:value org-id} ((:current-lang @state/translations) name)]))
               doall)]]])]
    [:div.search-filter
     [:h4 (t "Näytä vain")]
     [:div.filter-options
      [attachment-type-filter]
      [time/timespan]

      (when (seq @state/operations)
        [:div.filter-option
         [:label (t "Toimenpide")]
         [cb/combobox (into {} (map (fn [oper] {(t oper) oper}) @state/operations)) false :operation]])

      [time/closed-timespan]

      (cond
        (@state/selected-permit-types "R")
        [:div.filter-option
         [:label (t "Käyttötarkoitus")]

         (let [usage-map (into {} (map (fn [usage]
                                         (let [name (:name usage)]
                                           {(t name) name})) usages/rakennuksen-kayttotarkoitus))]
           [cb/combobox usage-map true :usage])]

        (:usage @state/search-query)
        (do (state/update-search-field :usage nil)
            nil))]

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
           [:span (t "Säilytysjärjestelmä")]]]]])
     (when onkalo-deleted-query
       [onkalo-deleted-query])]]])
