(ns cljsjs.pikaday
  "A shim to export npm pikaday as a global symbol for the old cljs-pikaday library."
  (:require ["pikaday" :as pikaday]))

(js/goog.exportSymbol "Pikaday" pikaday)
