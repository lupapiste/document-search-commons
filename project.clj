(defproject lupapiste/document-search-commons "0.5.1"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "http://www.lupapiste.fi"
  :license {:name "European Union Public License"
            :url "https://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11"
            :distribution :repo}
  :scm {:url "https://github.com/lupapiste/document-search-commons"}
  :repositories [["osgeo" {:url "http://download.osgeo.org/webdav/geotools"}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.geotools/gt-main "16.1"]
                 [org.geotools/gt-referencing "16.1"]
                 [org.geotools/gt-epsg-wkt "16.1"]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.9.36"]
                 [reagent "0.5.1"]
                 [cljs-ajax "0.5.5"]
                 [cljs-pikaday "0.1.2"]
                 [cljsjs/moment "2.10.6-4"]
                 [cljsjs/openlayers "3.15.1"]
                 [tailrecursion/cljson "1.0.7"]
                 [alandipert/storage-atom "2.0.1"]]
  :profiles {:provided {:dependencies [[lupapiste/commons "0.7.81" :exclusions [commons-logging commons-codec]]]}}
  :plugins [[deraen/lein-sass4clj "0.3.0"]]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljc" "src/cljs"]}}}
  :clean-targets ^{:protect false} ["resources/public/css/main.css.map"
                                    :target-path]
  :sass {:target-path  "resources/public/css/"
         :source-paths ["scss/"]
         :output-style :compressed})
