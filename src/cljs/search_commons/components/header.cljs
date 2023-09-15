(ns search-commons.components.header
  (:require-macros [search-commons.utils.macros :refer [handler-fn]])
  (:require [clojure.string :as string]
            [reagent.core :as reagent]
            [search-commons.routing :as routing]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

(defn display-name [user]
  (let [{:keys [firstName lastName]} user]
    (when firstName
      (str firstName " " lastName))))

(defn language-options [language-open?]
  (into
    [:ul.language-menu]
    (doall
      (for [lang [:fi :sv]]
        ^{:key lang} [:li
                      [:button.navi
                       {:on-click (handler-fn (state/set-lang! lang)
                                              (reset! language-open? false))}
                       (str (string/upper-case (name lang)) " - " (t lang))]]))))

(defn app-link [lang path]
  (str "/app/" lang "/" path))

(defn header [logo-style & extra-menu-items]
  (reagent/with-let [language-open? (reagent/atom false)
                     user-open?     (reagent/atom false)]
    (let [{:keys [user]} @state/config
          user-name (display-name user)
          lang      (name (-> @state/translations :current-lang))]
      [:nav.nav-wrapper
       [:div.nav-top
        [:div.nav-box
         [:div.brand
          [:a.logo.lupapiste-logo {:href  (app-link lang "authority#!/applications")
                                   :style logo-style}
           ""]]
         [:div#language-select.header-dropdown {:class (when @language-open? "active")}
          [:button.navi {:on-click (handler-fn (swap! language-open? not))}
           [:span (string/upper-case lang)]
           [:i {:class (if @language-open? "lupicon-chevron-small-up" "lupicon-chevron-small-down")}]]
          (when @language-open?
            [language-options language-open?])]
         (into
           [:div.header-menu]
           (concat
             extra-menu-items
             [[:div#header-user-dropdown.header-dropdown {:class (when @user-open? "active")}
               [:button.navi {:on-click #(swap! user-open? not)}
                [:i.lupicon-user]
                [:span#user-name
                 (or user-name (t "Ei käyttäjää"))]
                [:i {:class (if @user-open? "lupicon-chevron-small-up" "lupicon-chevron-small-down")}]]
               (when @user-open?
                 [:ul.user-dropdown
                  [:li
                   [:a.btn.navi {:href  (app-link lang "#!/mypage")
                                 :title (t "mypage.title")}
                    [:i.lupicon-user]
                    [:span (t "mypage.title")]]]
                  [:li
                   [:a.btn.navi {:href (app-link lang "logout") :title (t "logout")}
                    [:span.header-icon.lupicon-log-out]
                    [:span.narrow-hide (t "logout")]]]])]]))]]])))
