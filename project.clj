(defproject lupapiste/document-search-commons "2.0.14"
  :description "Common document search related code shared between lupadoku and onkalo applications"
  :url "https://www.lupapiste.fi"
  :license {:name         "European Union Public License"
            :url          "https://joinup.ec.europa.eu/community/eupl/og_page/european-union-public-licence-eupl-v11"
            :distribution :repo}
  :scm {:url "https://github.com/lupapiste/document-search-commons"}
  :repositories [["osgeo" {:url "https://repo.osgeo.org/repository/release/"}]]
  :dependencies [[org.clojure/clojure "1.11.1"]

                 ;; Coordinate tools
                 [org.eclipse.emf/org.eclipse.emf.common "2.29.0"]
                 [org.eclipse.emf/org.eclipse.emf.ecore "2.35.0" :exclusions [org.eclipse.emf/org.eclipse.emf.common]]
                 [org.geotools/gt-main "29.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                           org.eclipse.emf/org.eclipse.emf.ecore]]
                 [org.geotools/gt-referencing "29.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                                  org.eclipse.emf/org.eclipse.emf.ecore]]
                 [org.geotools/gt-epsg-wkt "29.2" :exclusions [org.eclipse.emf/org.eclipse.emf.common
                                                               org.eclipse.emf/org.eclipse.emf.ecore]]

                 ;; ClojureScript
                 [org.clojure/clojurescript "1.11.60"]
                 [reagent "1.2.0" :exclusions [org.clojure/clojurescript]]
                 [cljs-ajax "0.8.4"]
                 [cljs-pikaday "0.1.4" :exclusions [cljsjs/pikaday]]
                 [tailrecursion/cljson "1.0.7"]
                 [alandipert/storage-atom "2.0.1"]]
  :profiles {:provided {:dependencies [[lupapiste/commons "5.2.11"
                                        :exclusions [commons-logging commons-codec]]]}}
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj" "test/cljc"]
  :clean-targets ^{:protect false} ["resources/public/css/main.css.map"
                                    :target-path]
  :aliases {"extract-strings" ["run" "-m" "lupapiste-commons.i18n.extract/extract-strings" "t"]})
