(ns search-commons.components.dialog
  (:require [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.routing :as routing]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

(defn yes-no-with-exp-dialog-content [{:keys [message yes-fn yes-text] :as data}]
  (let [explanation (reagent/atom "")
        no-fn       (fn []
                      (reset! state/confirm-dialog-data nil)
                      (reset! state/show-confirm-dialog false))]
    (fn [{:keys [message yes-fn yes-text] :as data}]
      [:div.confirm-dialog-content
       [:div.message
        [:span.like-btn message]]
       [:div.explanation
        [:span (t "Perustelu (pakollinen) ")]
        [:input {:type        "text"
                 :placeholder (t "Perustelu")
                 :value       @explanation
                 :on-change   #(reset! explanation (.. % -target -value))}]]
       [:div.buttons
        [:button.negative {:on-click #(yes-fn @explanation)
                           :disabled (s/blank? @explanation)}
         [:span (or yes-text "OK")]]
        [:button.secondary {:on-click no-fn}
         [:span (t "Peruuta")]]]])))

(defn ok-dialog-content [{:keys [message ok-fn]}]
  (let [click-fn (fn []
                   (ok-fn)
                   (reset! state/confirm-dialog-data nil)
                   (reset! state/show-confirm-dialog false))]
    [:div.confirm-dialog-content
     [:div.message
      [:span.like-btn message]]
     [:div
      [:button.secondary {:on-click click-fn}]]]))

(defn confirm-dialog []
  (let [{:keys [dialog-type header] :as data} @state/confirm-dialog-data]
    (fn []
      [:div.confirm-dialog-background
       [:div.confirm-dialog
        [:div.confirm-dialog-header
         [:h4 header]]
        (cond
          (= dialog-type :ok) [ok-dialog-content data]
          (= dialog-type :yes-no-exp) [yes-no-with-exp-dialog-content data]
          :else (reset! state/show-confirm-dialog false))]])))

(defn ok-dialog [header message ok-fn]
  (swap! state/confirm-dialog-data :header header :message message :ok-fn ok-fn :dialog-type :ok)
  (reset! state/show-confirm-dialog true))

(defn yes-no-dialog-with-explanation [header message yes-fn & [yes-text]]
  (swap! state/confirm-dialog-data :header header :message message :yes-fn yes-fn :dialog-type :yes-no-exp)
  (when yes-text (swap! state/confirm-dialog-data :yes-text yes-text))
  (reset! state/show-confirm-dialog true))
