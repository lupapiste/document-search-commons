(defproject lupapiste/document-search-commons "1.1.1"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "https://www.lupapiste.fi"
  :license {:name "European Union Public License"
            :url "https://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11"
            :distribution :repo}
  :scm {:url "https://github.com/lupapiste/document-search-commons"}
  :repositories [["osgeo" {:url "https://repo.osgeo.org/repository/release/"}]]
  :dependencies [[org.clojure/clojure "1.10.3"]

                 ;; Coordinate tools
                 [org.eclipse.emf/org.eclipse.emf.common "2.22.0"]
                 [org.eclipse.emf/org.eclipse.emf.ecore "2.24.0" :exclusions [org.eclipse.emf/org.eclipse.emf.common]]
                 [org.geotools/gt-main "25.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                           org.eclipse.emf/org.eclipse.emf.ecore]]
                 [org.geotools/gt-referencing "25.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                                  org.eclipse.emf/org.eclipse.emf.ecore]]
                 [org.geotools/gt-epsg-wkt "25.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                               org.eclipse.emf/org.eclipse.emf.ecore]]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.10.879"]
                 [cljsjs/react "17.0.2-0"]
                 [cljsjs/react-dom "17.0.2-0"]
                 [reagent "1.1.0"]
                 [cljs-ajax "0.8.4"]
                 [cljs-pikaday "0.1.4"]
                 [cljsjs/moment "2.24.0-0"]
                 [cljsjs/openlayers "4.4.1-1"]
                 [tailrecursion/cljson "1.0.7"]
                 [alandipert/storage-atom "2.0.1"]]
  :profiles {:provided {:dependencies [[lupapiste/commons "5.1.2" :exclusions [commons-logging commons-codec]]]}}
  :plugins [[deraen/lein-sass4clj "0.3.1"]]
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj" "test/cljc"]
  :cljsbuild {:builds {:dev {:source-paths ["src/cljc" "src/cljs"]}}}
  :clean-targets ^{:protect false} ["resources/public/css/main.css.map"
                                    :target-path]
  :sass {:target-path  "resources/public/css/"
         :source-paths ["scss/"]
         :output-style :compressed}
  :aliases {"extract-strings" ["run" "-m" "lupapiste-commons.i18n.extract/extract-strings" "t"]})
