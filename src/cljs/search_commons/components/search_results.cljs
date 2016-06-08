(ns search-commons.components.search-results
  (:require [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]
            [search-commons.utils.utils :as utils]
            [reagent.core :as reagent]
            [goog.string :as gs]
            [goog.string.format]
            [search-commons.routing :as routing]
            [clojure.string :as s])
  (:import [goog.i18n DateTimeFormat]))

;; From sade.property in lupapalvelu
(def db-property-id-pattern
  "Regex for splitting db-saved property id to human readable form"
  #"^([0-9]{1,3})([0-9]{1,3})([0-9]{1,4})([0-9]{1,4})$")

(defn to-human-readable-property-id [property-id]
  (when property-id
    (->> (re-matches db-property-id-pattern property-id)
         rest
         (map js/parseInt)
         (s/join "-"))))

(defn format-file-extension [filename]
  (let [parts (s/split filename #"\.")]
    (if (= (count parts) 1)
      "?"
      (s/upper-case (str "." (last parts))))))

(defn format-date [ts]
  (.format (DateTimeFormat. "d.M.yyyy") (js/Date. ts)))

(defn cancel-search-param [remove-fn]
  [:i.icon-cancel-circled {:on-click (fn []
                                       (remove-fn)
                                       (state/new-search))}])

(defn format-search-time [took onkalo-took]
  (let [took-str (when took
                   (str (t "Asiointi") (gs/format " %.1f s" took)))
        onkalo-took-str (when onkalo-took
                          (str (t "Säilytysjärjestelmä") (gs/format " %.1f s" onkalo-took)))]
    (s/join ", " (remove nil? [took-str onkalo-took-str]))))

(defn result-summary []
  (let [{:keys [took onkalo-took has-more? onkalo-has-more?]} @state/search-results]
    [:div.result-summary
     [:div.result-summary-text
      [:div.result-count (t "Hakutulokset") " (" (when (or has-more? onkalo-has-more?) (str (t "yli") " ")) @state/total-result-count ")"]
      [:div.took (format-search-time took onkalo-took)]]
     (let [{:keys [fields type timespan operation usage]} @state/last-search]
       [:div.search-params
        (doall
          (for [field fields]
            [:div {:key field} (t "Kentät") ": " (t (name field))
             [cancel-search-param #(swap! state/search-query assoc :fields (disj fields field))]]))
        (when (seq type)
          [:div (t "Asiakirjatyyppi") ": " (t (s/join "." (map name type)))
           [cancel-search-param #(swap! state/search-query assoc :type '())]])
        (when (:from timespan)
          [:div (t "Alkaen") ": " (format-date (:from timespan))
           [cancel-search-param #(reset! state/start-date nil)]])
        (when (:to timespan)
          [:div (t "Päättyen") ": " (format-date (:to timespan))
           [cancel-search-param #(reset! state/end-date nil)]])
        (when operation
          [:div (t "Toimenpide") ": " (t operation)
           [cancel-search-param #(swap! state/search-query assoc :operation nil)]])
        (when usage
          [:div (t "Käyttötarkoitus") ": " (t usage)
           [cancel-search-param #(swap! state/search-query assoc :usage nil)]])
        (when (seq @state/map-selected-result-ids)
          [:div (t "Karttavalinta")
           [:i.icon-cancel-circled {:on-click (fn []
                                                (reset! state/map-selected-result-ids #{}))}]])])]))
(defn municipality-name [code]
  (t (str "municipality." code)))

(defn type-str [{:keys [type-group type-id] :as type}]
  (if type-group (str type-group "." type-id) type))

(defn result-list-item [result]
  (let [{:keys [propertyId address verdict-ts municipality type contents id filename created tiedostonimi paatospvm metadata]} result
        verdict-date (or verdict-ts paatospvm)]
    [:li.result-item {:on-click (fn []
                                  (reset! state/selected-result-id id)
                                  (state/mark-result-seen id))}
     [:div.result-item-data {:class (when (= result @state/selected-result) "selected")}
      [:div.result-type
       (format-file-extension (or filename tiedostonimi))
       (if (contains? (:seen-results @state/search-results) id)
         [:div.seen-icon
          [:i.icon-ok-circled2 {:title (t "Tulos katsottu")}]]
         [:div.result-item-status (t (:tila metadata))])]
      [:div.result-item-text
       [:div.hover-popout

        [:h4 (t (type-str type))]
        [:div.document-contents contents]
        [:div
         (when-not (s/blank? address)
           [:span address ", "])
         [:span
          (municipality-name municipality) " - " (to-human-readable-property-id propertyId) " - "
          (let [ts (or verdict-date created)
                t-key (if verdict-date "Päätetty {pvm}" "Lisätty {pvm}")]
            (-> (t t-key)
                (.replace "{pvm}" (format-date ts))))]]]]]
     (when (= id @state/selected-result-id)
       [:div.arrow-right])]))

(defn result-list []
  (let [{:keys [has-more? onkalo-has-more? loading?]} @state/search-results
        {{:keys [lupapiste-host]} :config} @state/config]
    [:div.result-list-container
     [:ol.result-list
      (doall
        (for [[grouping-key result-group] @state/result-groups]
          (let [{:keys [applicationId]} (first result-group)]
            ^{:key grouping-key}
            [:li.result-application
             [:h4.application-separator
              (if applicationId
                [:a {:href (str lupapiste-host "/app/fi/authority#!/application/" applicationId)} applicationId]
                [:span grouping-key])]
             [:ol.result-list
              (for [result result-group]
                ^{:key (str (:id result) "-list-item")}
                [result-list-item result])]])))]
     (when (or has-more? onkalo-has-more?)
       [:div.fetch-more
        (if loading?
          [:i.icon-spin5.animate-spin]
          [:button {:on-click state/fetch-more}
           (t "Lisää tuloksia")])])]))

(defn parse-margin [v]
  (if (s/blank? v)
    0
    (if (number? v)
      v
      (js/parseInt (s/replace v "px" "")))))

(defn animate-view-transition [component]
  (when-let [node (reagent/dom-node component)]
    (let [parent-offset-top (.-offsetTop (.-parentNode (.-parentNode node)))
          scroll-y (or (.-scrollY js/window) (.-pageYOffset js/window))
          target-top (max (+ (- scroll-y parent-offset-top) 5) 0)
          current-top (parse-margin (-> node .-style .-marginTop))
          diff (- target-top current-top)
          duration 2001.0
          epsilon (/ 1000 60 duration 4)
          ease-fn (utils/bezier 0.25 1 0.25 1 epsilon)]
      (if utils/is-old-ie?
        (set! (-> node .-style .-marginTop) (str target-top "px"))
        (.requestAnimationFrame js/window (fn [start]
                                            (letfn [(animate-frame [ts] (let [dt (- ts start)
                                                                              progress (* 1.05 (/ dt duration))
                                                                              bezier-term (ease-fn progress)
                                                                              position (+ current-top (Math/round (* bezier-term diff)))]
                                                                          (set! (-> node .-style .-marginTop) (str position "px"))
                                                                          (when-not (= position target-top)
                                                                            (.requestAnimationFrame js/window animate-frame))))]
                                              (animate-frame start))))))))

(defn search-status []
  [:div.loading
   (if (:loading? @state/search-results)
     [:img {:src (routing/path "/img/ajax-loader.gif")}]
     [:span.result-summary-text (t "Ei hakutuloksia")])])

(defn result-section [document-view result-view]
  (fn [document-view result-view]
    [:div.result-section
     [document-view]
     [result-summary]
     (if-not (zero? @state/total-result-count)
       [:div
        [:div.result-list-and-item
         [result-list]
         [:div.result-view-container
          [result-view]]]]
       [search-status])]))
