(ns search-commons.authorization
  (:require [lupapiste-commons.ring.session-timeout :as commons-session-timeout]
            [search-commons.i18n :refer [t]]))


(defn unauthorized-response [lang-data session]
  {:status 401
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body (t "Sisäänkirjautuminen vaaditaan" lang-data)
   :session session})

(defn forbidden-response [lang-data session]
  {:status 403
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body (t "Sinulla ei ole tarvittavia käyttöoikeuksia" lang-data)
   :session session})

(defn redirect-response [redirect-path]
  {:status  302
   :headers {"Location" "/login/fi"}
   :session {:redirect-after-login redirect-path}})

(defn user-is-authorized? [user required-role]
  (seq (filter (fn [[_ v]] (required-role v))
               (:orgAuthz user))))

(defn wrap-user-authorization [handler tr-data required-roles & [redirect-path]]
  (fn [request]
    (let [lang (or (keyword (get-in request [:headers "Accept-Language"])) :fi)]
      (if-let [user (and (not (commons-session-timeout/session-expired? request))
                         (or (get-in request [:session :user])
                             (:autologin-user request)))]
        (if (not (every? nil? (mapv (fn [role] (user-is-authorized? user role)) required-roles)))
          (let [response (-> (assoc request :user user)
                             (handler))]
            (when response
              (assoc response :session (-> (or (:session response) (:session request))
                                           (dissoc :redirect-after-login)))))
          (forbidden-response (lang tr-data) (dissoc (:session request) :redirect-after-login)))
        (if redirect-path
          (redirect-response redirect-path)
          (unauthorized-response (lang tr-data) (:session request)))))))
