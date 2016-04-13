(ns search-commons.components.map
  (:require [ol]
            [ol.Attribution]
            [ol.Collection]
            [ol.control]
            [ol.control.Control]
            [ol.Map]
            [ol.View]
            [ol.extent]
            [ol.interaction.DragPan]
            [ol.interaction.Draw]
            [ol.interaction.MouseWheelZoom]
            [ol.interaction.Select]
            [ol.layer.Tile]
            [ol.layer.Vector]
            [ol.proj]
            [ol.source.Vector]
            [ol.source.WMTS]
            [ol.source.Cluster]
            [ol.style.Circle]
            [ol.style.Fill]
            [ol.style.Stroke]
            [ol.style.Style]
            [ol.style.Text]
            [ol.style.Icon]
            [ol.tilegrid.WMTS]
            [ajax.core :refer [GET]]
            [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]
            [search-commons.routing :as routing]))

(def suomen-extent
  "Suomalaisissa kartoissa olevan projektion raja-arvot."
  [-548576.000000, 6291456.000000, 1548576.000000, 8388608.000000])

(def projektio (ol.proj/Projection. #js {:code "EPSG:3067"
                                         :extent (clj->js suomen-extent)
                                         :unit "m"}))

(def db-property-id-regex #"(\d{1,3})(\d{1,3})(\d{1,4})(\d{1,4})")

(defn proxy-url [map]
  (let [host (get-in @state/config [:config :lupapiste-host])]
    (str host "/proxy/" map)))

(defn make-tilegrid []
  (let [koko (/ (ol.extent/getWidth (.getExtent projektio)) 256)]
    (loop [resoluutiot []
           matrix-idt []
           i 0]
      (if (= i 16)
        (let [optiot (clj->js {:origin (ol.extent/getTopLeft (.getExtent projektio))
                               :resolutions (clj->js resoluutiot)
                               :matrixIds (clj->js matrix-idt)})]
          (ol.tilegrid.WMTS. optiot))
        (recur (conj resoluutiot (/ koko (Math/pow 2 i)))
               (conj matrix-idt i)
               (inc i))))))

(defn map-center []
  (if-let [coords (seq (:coordinates @state/search-query))]
    (ffirst coords)
    (get-in @state/config [:user :default-map-coordinates])))

(defn human-readble-property-id [id]
  (s/join "-" (map #(js/parseInt %) (vec (rest (first (re-seq db-property-id-regex id)))))))

(defn get-property-id [[x y]]
  (state/update-search-field :text nil)
  (GET (proxy-url "property-id-by-point")
       {:params {:x x :y y}
        :handler (fn [response]
                   (state/update-search-field :text (human-readble-property-id response)))}))


(defn map-click [event]
  (get-property-id (-> event .-coordinate js->clj)))

(defn make-features []
  (map (fn [{id :id [x y] :location}] (ol.Feature. #js {:geometry (ol.geom.Point. #js [x y])
                                                        :file-id id})) @state/results-for-map))

(defn make-cluster-source []
  (let [point-features (ol.Collection. (clj->js (make-features)))
        point-source (ol.source.Vector. #js {:features point-features})]
    (ol.source.Cluster. #js {:distance 40
                             :source point-source})))

(def style-cache (atom {}))

(defn single-item-style []
  {:image (ol.style.Icon. #js {:src (routing/path "/img/map-marker-big.png")})})

(defn cluster-style [selected? size]
  {:image (ol.style.Circle. #js {:radius 18
                                 :stroke (ol.style.Stroke. #js {:color "#303030" :width 3})
                                 :fill (ol.style.Fill. #js {:color (if selected? "#FFE236" "#F79226")})})
   :text (ol.style.Text. #js {:text (str size)
                              :font "18px 'Source Sans Pro', sans-serif"
                              :fill (ol.style.Fill. #js {:color "#000"})})})

(defn get-cluster-style [selected? feature]
  (let [size (-> feature (.get "features") (.-length))
        cache-key (str size selected?)]
    (or (get @style-cache cache-key)
        (-> (->> (ol.style.Style. (clj->js (cluster-style selected? size)))
                 (swap! style-cache assoc cache-key))
            (get cache-key)))))

(def cluster-layer (ol.layer.Vector. #js {:source (make-cluster-source)
                                          :style  (partial get-cluster-style false)}))

(def map-view-atom (atom nil))

(def map-object-atom (atom nil))

(defn fit-map [src view map-obj]
  (let [source-extent (.getExtent (.getSource src))]
    (when-not (some #(#{js/Infinity 0} (.abs js/Math %)) source-extent)
      (.fit view source-extent (.getSize map-obj) #js {:minResolution 1
                                                       :padding #js [20 20 20 20]}))))

(defn update-map-features [component]
  (let [coordinates (-> component reagent/props :data)]
    (if (seq coordinates)
      (let [new-source (make-cluster-source)]
        (.setSource cluster-layer new-source)
        (fit-map new-source @map-view-atom @map-object-atom))
      (.setSource cluster-layer nil))))

(def drawing-enabled? (atom true))

(defn ol-map []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [map-layer (ol.layer.Tile. #js {:source (ol.source.WMTS.
                                                      #js {:url             (proxy-url "wmts/maasto")
                                                           :attributions    #js [(ol.Attribution. #js {:html "MML"})]
                                                           :matrixSet       "ETRS-TM35FIN"
                                                           :style           "default"
                                                           :projection      "EPSG:3067"
                                                           :format          "image/png"
                                                           :tileGrid        (make-tilegrid)
                                                           :layer           "taustakartta"
                                                           :requestEncoding "KVP"})})
             map-layer-kiinteisto (ol.layer.Tile. #js {:source        (ol.source.WMTS.
                                                                        #js {:url             (proxy-url "wmts/kiinteisto")
                                                                             :attributions    #js [(ol.Attribution. #js {:html "MML"})] :matrixSet "ETRS-TM35FIN"
                                                                             :style           "default"
                                                                             :projection      "EPSG:3067"
                                                                             :format          "image/png"
                                                                             :tileGrid        (make-tilegrid)
                                                                             :layer           "kiinteistojaotus"
                                                                             :requestEncoding "KVP"})
                                                       :maxResolution 4})

             drawing-source (ol.source.Vector. #js {:wrapX    false
                                                    :features #js [(ol.Feature. #js {:geometry (ol.geom.Polygon. (clj->js (:coordinates @state/search-query)))})]})

             drawing-layer (ol.layer.Vector. #js {:source drawing-source
                                                  :style  (ol.style.Style. #js {:fill   (ol.style.Fill. #js {:color "rgba(255, 255, 255, 0.2)"})
                                                                                :stroke (ol.style.Stroke. #js {:color "#ffcc33" :width 2})
                                                                                :image  (ol.style.Circle. #js {:radius 7 :fill (ol.style.Fill. #js {:color "#ffcc33"})})})})
             features (doto (ol.Collection.)
                        (.on "add" (fn [event]
                                     (swap! state/search-query update-in [:coordinates] conj (-> event
                                                                                                 .-element
                                                                                                 .getGeometry
                                                                                                 .getCoordinates
                                                                                                 js->clj
                                                                                                 first))
                                     (.preventDefault event))))

             drawing-interaction (ol.interaction.Draw. #js {:source   drawing-source
                                                            :type     "Polygon"
                                                            :features features
                                                            :condition (fn [event]
                                                                         (not (.hasFeatureAtPixel @map-object-atom (.-pixel event) (fn [layer-candidate] (= layer-candidate cluster-layer)))))})

             select-interaction (ol.interaction.Select. #js {:multi true
                                                             :layers #js [cluster-layer]
                                                             :style (partial get-cluster-style true)})

             interactions (ol.Collection. #js [(ol.interaction.DragPan.) (ol.interaction.MouseWheelZoom.) select-interaction drawing-interaction])

             custom-buttons (let [class (.createAttribute js/document "class")
                                  iclass (.createAttribute js/document "class")
                                  div (.createElement js/document "div")
                                  button (.createElement js/document "button")
                                  txt (.createTextNode js/document "")]
                              (.appendChild button txt)
                              (.appendChild div button)
                              (set! (.-value class) "ol-full-screen ol-unselectable ol-control")
                              (set! (.-value iclass) "icon-flow-line")
                              (.setAttributeNode div class)
                              (.setAttributeNode button iclass)
                              (.setAttribute button "title" (t "Rajaa hakualue"))
                              (.addEventListener button "click"
                                                 (fn [evt]
                                                   (swap! drawing-enabled? not)
                                                   (let [new-class (if @drawing-enabled? "icon-flow-line" "icon-location")
                                                         new-title (if @drawing-enabled? (t "Rajaa hakualue") (t "Poimi kiinteistörekisterinumero"))]
                                                     (.clear drawing-source)
                                                     (swap! state/search-query assoc :coordinates [])
                                                     (if @drawing-enabled?
                                                       (.push interactions drawing-interaction)
                                                       (.pop interactions))
                                                     (set! (.-className button) new-class)
                                                     (.setAttribute button "title" new-title)
                                                     (.preventDefault evt))))
                              div)
             cluster-source (make-cluster-source)
             selected-features (.getFeatures select-interaction)]
         (.setSource cluster-layer cluster-source)
         (reset! map-view-atom (ol.View. #js {:center     (clj->js (map-center))
                                              :zoom       8
                                              :projection projektio}))
         (reset! map-object-atom (ol/Map. #js {:controls     (-> (ol.control.defaults) (.extend (clj->js [(ol.control.Control. #js {"element" custom-buttons})])))
                                               :target       "map"
                                               :view         @map-view-atom
                                               :layers       #js [map-layer map-layer-kiinteisto drawing-layer cluster-layer]
                                               :interactions interactions}))

         (.on selected-features "add" (fn [event] (let [cluster (-> event .-target (.item 0))
                                                        features (.get cluster "features")]
                                                      (doseq [feature features]
                                                        (println (.get feature "file-id"))))))

         (.on @map-object-atom "singleclick" (fn [event]
                                               (when-not (or @drawing-enabled?
                                                             (.hasFeatureAtPixel @map-object-atom (.-pixel event) (fn [layer-candidate] (= layer-candidate cluster-layer))))
                                                 (map-click event))))
         (fit-map cluster-source @map-view-atom @map-object-atom)))

     :reagent-render
     (fn [this]
       [:div.map {:id "map"}])
     :component-did-update update-map-features}))
