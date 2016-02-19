(defproject lupapiste/document-search-commons "0.1.0"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "http://www.lupapiste.fi"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lupapiste/commons "0.6.51"]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.7.228"]
                 [reagent "0.5.1"]
                 [cljs-ajax "0.5.3"]
                 [cljs-pikaday "0.1.2"]
                 [cljsjs/moment "2.10.6-2"]
                 [cljsjs/openlayers "3.13.0"]
                 [tailrecursion/cljson "1.0.7"]]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljc" "src/cljs"]}}})
