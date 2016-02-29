(defproject lupapiste/document-search-commons "0.1.6"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "http://www.lupapiste.fi"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo}
  :scm {:url "https://deus.solita.fi/Solita/projects/lupapiste/repositories/document-search-commons/tree/master"}
  :repositories [["solita" "http://mvn.solita.fi/repository/solita"]
                 ["osgeo" {:url "http://download.osgeo.org/webdav/geotools"}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [lupapiste/commons "0.6.51"]
                 [org.geotools/gt-main "14.2"]
                 [org.geotools/gt-referencing "14.2"]
                 [org.geotools/gt-epsg-wkt "14.2"]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.7.228"]
                 [reagent "0.5.1"]
                 [cljs-ajax "0.5.3"]
                 [cljs-pikaday "0.1.2"]
                 [cljsjs/moment "2.10.6-2"]
                 [cljsjs/openlayers "3.13.0"]
                 [tailrecursion/cljson "1.0.7"]]
  :plugins [[lein-scss "0.2.3" :exclusions [org.clojure/clojure]]]
  :aot :all
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljc" "src/cljs"]}}}
  :clean-targets ^{:protect false} ["resources/public/css/main.css.map"
                                    :target-path]
  :scss {:builds
         {:dev  {:source-dir "scss/"
                 :dest-dir   "resources/public/css/"
                 :executable "sass"
                 :args       ["-r" "compass-core" "-t" "nested" "--compass" "--sourcemap=file"]}
          :prod {:source-dir "scss/"
                 :dest-dir   "resources/public/css/"
                 :executable "sass"
                 :args       ["-r" "compass-core" "-t" "compressed" "--compass" "--sourcemap=none"]}}})
