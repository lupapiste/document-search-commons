(ns search-commons.components.footer
  (:require [search-commons.utils.i18n :refer [t]]))

(defn footer []
  [:footer
   [:div.informations
    [:div.footer-box
     [:span.footer-title (t "footer.conversation.title")]
     [:p (t "footer.conversation.desc")]]
    [:div.footer-box
     [:span.footer-title (t "footer.report-issue.title")]
     [:p
      [:span (t "footer.report-issue.desc")]
      [:span
       [:a {:href (t "footer.report-issue.link-href")} (t "footer.report-issue.link-text")]]]]
    [:div.footer-box
     [:ul
      [:li
       [:a {:href (t "footer.register.link-href")} (t "footer.register")]]
      [:li
       [:a {:href (t "footer.terms.link-href")} (t "footer.terms")]]
      [:li
       [:a {:href (t "footer.license.link-href")} (t "footer.license")]]]]
    [:div.footer-box
     [:p]]]])
