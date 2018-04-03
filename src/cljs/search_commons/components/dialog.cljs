(ns search-commons.components.dialog
  (:require [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.routing :as routing]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

(defn yes-no-with-exp-dialog-content [{:keys [message yes-fn yes-text] :as data}]
  (let [explanation (reagent/atom "")
        waiting? (reagent/atom false)
        yes-fn-with-wait (fn [expl]
                           (reset! waiting? true)
                           (yes-fn expl))
        no-fn (fn []
                (reset! state/dialog-data nil)]
    (fn [{:keys [message yes-fn yes-text] :as data}]
      [:div.dialog-content
       [:div.message
        [:span.like-btn message]]
       [:div.explanation
        [:span (t "Perustelu (pakollinen) ")]
        [:input {:type        "text"
                 :placeholder (t "Perustelu")
                 :value       @explanation
                 :on-change   #(reset! explanation (.. % -target -value))}]]
       [:div.buttons
        [:button.btn-dialog.negative {:on-click #(yes-fn-with-wait @explanation)
                           :class (when @waiting? "waiting")
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
          :else (reset! state/show-dialog false))]])))

(defn ok-dialog [header message ok-fn]
  (reset! state/dialog-data nil)
  (swap! state/dialog-data assoc :header header :message message :ok-fn ok-fn :dialog-type :ok))

(defn yes-no-dialog-with-explanation [header message yes-fn & [yes-text]]
  (reset! state/dialog-data nil)
  (swap! state/dialog-data assoc :header header :message message :yes-fn yes-fn :dialog-type :yes-no-exp)
  (when yes-text (swap! state/dialog-data assoc :yes-text yes-text)))
