(ns search-commons.routing)

(def app-root (atom ""))

(defn set-root [root]
  (reset! app-root root))

(defn path [name]
  (str @app-root name))
