(ns search-commons.authorization
  (:require [clojure.set :refer [intersection]]
            [search-commons.i18n :refer [t]]))

(defn unauthorized-response [lang-data]
  {:status 401
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body (t "Sisäänkirjautuminen vaaditaan" lang-data)
   :session {:redirect-after-login "/document-search"}})

(defn forbidden-response [lang-data]
  {:status 403
   :headers {"Content-Type" "text/plain; charset=utf-8"}
   :body (t "Sinulla ei ole tarvittavia käyttöoikeuksia" lang-data)})

(defn user-is-authorized? [user]
  (seq (filter (fn [[_ v]] (:authority v))
               (:orgAuthz user))))

(defn wrap-user-authorization [handler tr-data]
  (fn [request]
    (let [lang (or (keyword (get-in request [:headers "Accept-Language"])) :fi)]
      (if-let [user (get-in request [:session :user])]
        (if (user-is-authorized? user)
          (let [response (handler request)]
            (when response
              (assoc response :session (-> (or (:session response) (:session request))
                                           (dissoc :redirect-after-login)))))
          (forbidden-response (lang tr-data)))
        (unauthorized-response (lang tr-data))))))

