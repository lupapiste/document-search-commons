(ns search-commons.utils.macros)

(defmacro handler-fn
  ([& body]
   `(fn [~'event] ~@body nil)))
