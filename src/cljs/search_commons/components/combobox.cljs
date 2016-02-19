(ns search-commons.components.combobox
  (:require [reagent.core :as reagent]
            [clojure.string :as string]
            [search-commons.utils.state :as state]))

(defn str-starts-with? [str start]
  (= (.indexOf (.toLowerCase str) (.toLowerCase start)) 0))

(defn str-contains? [str part]
  (>= (.indexOf (.toLowerCase str) (.toLowerCase part)) 0))

(defn find-text-for-value [option-value-map v]
  (when v
    (->> option-value-map
         (filter (fn [[_ value]] (= value v)))
         (first)
         (first))))

(defn combobox [option-value-map number-of-results match-anywhere? value-kw]
  (let [results (reagent/atom [])
        start-value (get @state/search-query value-kw)
        start-text (find-text-for-value option-value-map start-value)
        input-value (reagent/atom (or start-text ""))]
    (add-watch state/search-query value-kw (fn [_ _ o n]
                                             (when (and (seq (get o value-kw)) (empty? (get n value-kw)))
                                               (reset! input-value ""))))
    (fn [option-value-map number-of-results match-anywhere? value-kw]
      [:div.combobox
       [:div.combobox-input
        [:input {:value @input-value
                 :on-change (fn [e]
                              (let [text (.. e -target -value)
                                    match-fn (if match-anywhere? str-contains? str-starts-with?)
                                    matches (if (string/blank? text)
                                              []
                                              (take number-of-results (filter (fn [[k _]] (match-fn k text)) option-value-map)))
                                    sorted-matches (sort-by first matches)]
                                (reset! input-value text)
                                (if (and (= (count sorted-matches) 1) (= (.toLowerCase (first (first sorted-matches))) (.toLowerCase text)))
                                  (do (reset! results [])
                                      (state/update-search-field value-kw (last (first sorted-matches))))
                                  (reset! results sorted-matches))
                                (when (string/blank? text)
                                  (state/update-search-field value-kw nil))))}]]
       (when (seq @results)
         [:div.combobox-results
          [:ul
           (doall
             (for [[text value] @results]
               [:li {:key      value
                     :on-click (fn []
                                 (reset! input-value text)
                                 (reset! results [])
                                 (state/update-search-field value-kw value))} text]))]])])))
