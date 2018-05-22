(ns search-commons.components.dialog
  (:require [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.routing :as routing]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

(defn yes-no-with-exp-dialog-content [{:keys [message yes-fn yes-button yes-text] :as data}]
  (let [explanation (reagent/atom "")
        waiting? (reagent/atom false)
        yes-fn-with-wait (fn [expl]
                           (reset! waiting? true)
                           (yes-fn expl))
        yes-button-class (str "btn-dialog " yes-button)
        no-fn (fn []
                (reset! state/dialog-data nil))]
    (fn [{:keys [message yes-fn yes-button yes-text] :as data}]
      [:div.dialog-content
       [:div.message
        [:span.like-btn message]]
       [:div.explanation
        [:div.label
         [:label {:class (if (s/blank? @explanation) "form-label tip" "form-label")} (t "Perustelu")]]
        [:div.textarea
         [:textarea {:class (when (s/blank? @explanation) "tip")
                     :cols        "70"
                     :rows        "5"
                     :auto-focus  true
                     :placeholder (t "Perustelu")
                     :value       @explanation
                     :on-change   #(reset! explanation (.. % -target -value))
                     :read-only   @waiting?}]]]
       [:div.buttons
        [:button {:on-click #(yes-fn-with-wait @explanation)
                  :class yes-button-class
                  :disabled (or @waiting? (s/blank? @explanation))}
         [:span (or yes-text "OK")]]
        [:button.btn-dialog.secondary {:on-click no-fn
                                       :disabled @waiting?}
         [:span (t "Peruuta")]]]])))

(defn ok-dialog-content [{:keys [message ok-fn]}]
  (let [click-fn (fn []
                   (reset! state/dialog-data nil)
                   (ok-fn))]
    (fn []
      [:div.dialog-content
       [:div.message
        [:span.like-btn message]]
       [:div.ok
        [:button.btn-dialog.secondary {:on-click click-fn}
         [:span (t "OK")]]]])))

(defn dialog []
  (fn []
    (let [{:keys [dialog-type header] :as data} @state/dialog-data]
      [:div.dialog-background
       [:div.dialog
        [:div.dialog-header
         [:p header]]
        (cond
          (= dialog-type :ok) [ok-dialog-content data]
          (= dialog-type :yes-no-exp) [yes-no-with-exp-dialog-content data]
          :else (reset! state/dialog-data nil))]])))

(defn ok-dialog [header message ok-fn]
  (reset! state/dialog-data nil)
  (swap! state/dialog-data assoc :header header :message message :ok-fn ok-fn :dialog-type :ok))

(defn yes-no-dialog-with-explanation [header message yes-fn yes-button & [yes-text]]
  (reset! state/dialog-data nil)
  (swap! state/dialog-data assoc :header header :message message :yes-fn yes-fn :yes-button yes-button :dialog-type :yes-no-exp)
  (when yes-text (swap! state/dialog-data assoc :yes-text yes-text)))
