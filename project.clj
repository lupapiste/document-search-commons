(defproject lupapiste/document-search-commons "0.8.7"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "https://www.evolta.fi"
  :license {:name "European Union Public License"
            :url "https://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11"
            :distribution :repo}
  :scm {:url "https://github.com/lupapiste/document-search-commons"}
  :repositories [["osgeo" {:url "https://download.osgeo.org/webdav/geotools"}]]
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.geotools/gt-main "19.1"]
                 [org.geotools/gt-referencing "19.1"]
                 [org.geotools/gt-epsg-wkt "19.1"]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.8.1"]
                 [cljs-ajax "0.7.3"]
                 [cljs-pikaday "0.1.4"]
                 [cljsjs/moment "2.22.2-0"]
                 [cljsjs/openlayers "4.4.1-1"]
                 [tailrecursion/cljson "1.0.7"]
                 [alandipert/storage-atom "2.0.1"]]
  :profiles {:provided {:dependencies [[lupapiste/commons "0.9.26" :exclusions [commons-logging commons-codec]]]}}
  :plugins [[deraen/lein-sass4clj "0.3.1"]]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljc" "src/cljs"]}}}
  :clean-targets ^{:protect false} ["resources/public/css/main.css.map"
                                    :target-path]
  :sass {:target-path  "resources/public/css/"
         :source-paths ["scss/"]
         :output-style :compressed}
  :aliases {"extract-strings" ["run" "-m" "lupapiste-commons.i18n.extract/extract-strings" "t"]})
