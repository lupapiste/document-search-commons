(ns search-commons.components.combobox
  (:require [reagent.core :as reagent]
            [clojure.string :as s]
            [search-commons.utils.state :as state])
  (:require-macros [search-commons.utils.macros :refer [handler-fn]]))

(defn find-text-for-value [option-value-map v]
  (when v
    (->> option-value-map
         (filter (fn [[_ value]] (= value v)))
         (ffirst))))

(defn sorted-results [option-map & [text match-anywhere?]]
  (let [lc-text (when text (s/lower-case text))]
    (cond->> option-map
             lc-text (filter
                       (fn [[k _]]
                         (if match-anywhere?
                           (s/includes? (.toLowerCase k) lc-text)
                           (s/starts-with? (.toLowerCase k) lc-text))))
             :always (sort-by first))))

(defn combobox [option-value-map match-anywhere? value-kw]
  (let [results (reagent/atom [])
        start-value (get @state/search-query value-kw)
        start-text (find-text-for-value option-value-map start-value)
        input-value (reagent/atom (or start-text ""))
        results-visible? (reagent/atom false)]
    (add-watch state/search-query value-kw (fn [_ _ o n]
                                             (when (and (seq (get o value-kw)) (empty? (get n value-kw)))
                                               (reset! input-value ""))))
    (fn [option-value-map match-anywhere? value-kw]
      [:div.combobox
       [:div.combobox-input
        [:input {:value @input-value
                 :on-focus (handler-fn
                             (reset! results (sorted-results option-value-map @input-value match-anywhere?))
                             (reset! results-visible? true))
                 :on-blur (handler-fn (reset! results-visible? false))
                 :on-change (fn [e]
                              (let [text (.. e -target -value)
                                    sorted-matches (sorted-results option-value-map text match-anywhere?)]
                                (reset! input-value text)
                                (if (and (= (count sorted-matches) 1) (= (.toLowerCase (ffirst sorted-matches)) (.toLowerCase text)))
                                  (do (reset! results-visible? false)
                                      (state/update-search-field value-kw (last (first sorted-matches))))
                                  (do (reset! results-visible? true)
                                      (reset! results sorted-matches)))
                                (when (s/blank? text)
                                  (state/update-search-field value-kw nil))))}]]
       (when @results-visible?
         [:div.combobox-results
          [:ul
           (doall
             (for [[text value] @results]
               [:li {:key      value
                     :on-mouse-down (fn []
                                      (reset! input-value text)
                                      (reset! results-visible? false)
                                      (state/update-search-field value-kw value))} text]))]])])))
