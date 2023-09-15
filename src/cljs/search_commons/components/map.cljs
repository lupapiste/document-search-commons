(ns search-commons.components.map
  (:require ["ol/control/Attribution" :default Attribution]
            ["ol/Collection" :default Collection]
            ["ol/Feature" :default Feature]
            ["ol/control" :as ol.control]
            ["ol/control/Control$default" :as Control]
            ["ol/geom/Point" :default Point]
            ["ol/geom/Polygon" :default Polygon]
            ["ol/Map" :default Map]
            ["ol/View" :default View]
            ["ol/extent" :as ol.extent]
            ["ol/interaction" :refer (DragPan Draw MouseWheelZoom Select)]
            ["ol/layer/Tile" :default TileLayer]
            ["ol/layer/Vector" :default VectorLayer]
            ["ol/proj/Projection" :default Projection]
            ["ol/source/Vector" :default VectorSource]
            ["ol/source/WMTS" :default WMTS]
            ["ol/source/Cluster" :default Cluster]
            ["ol/style" :refer (Circle Fill Icon Stroke Style Text)]
            ["ol/tilegrid/WMTS" :default WMTSTileGrid]
            [ajax.core :refer [GET]]
            [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.routing :as routing]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

(def suomen-extent
  "Suomalaisissa kartoissa olevan projektion raja-arvot."
  [-548576.000000, 6291456.000000, 1548576.000000, 8388608.000000])

(def projektio (Projection. #js {:code "EPSG:3067"
                                 :extent (clj->js suomen-extent)
                                 :unit "m"}))

(def db-property-id-regex #"(\d{1,3})(\d{1,3})(\d{1,4})(\d{1,4})")

(defn proxy-url [map]
  (let [host (get-in @state/config [:config :cdn-host])]
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
          (WMTSTileGrid. optiot))
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
  (map (fn [{id :id [x y] :location}] (Feature. #js {:geometry (Point. #js [x y])
                                                        :file-id id})) @state/results-for-map))

(defn make-cluster-source []
  (let [point-features (Collection. (clj->js (make-features)))
        point-source (VectorSource. #js {:features point-features})]
    (Cluster. #js {:distance 40
                             :source point-source})))

(def style-cache (atom {}))

(defn single-item-style []
  {:image (Icon. #js {:src (routing/path "/img/map-marker-big.png")})})

(def selected-marker-color "rgb(255,82,27)")
(def marker-color "#4f0891")

(defn cluster-style [selected? size]
  {:image (Circle. #js {:radius 18
                        :fill   (Fill. #js {:color (if selected? selected-marker-color marker-color)})})
   :text  (Text. #js {:text (str size)
                      :font "18px 'Source Sans Pro', sans-serif"
                      :fill (Fill. #js {:color "#fff"})})})

(defn get-cluster-style [selected? feature]
  (let [size (-> feature (.get "features") (.-length))
        cache-key (str size selected?)]
    (or (get @style-cache cache-key)
        (-> (->> (Style. (clj->js (cluster-style selected? size)))
                 (swap! style-cache assoc cache-key))
            (get cache-key)))))

(def cluster-layer (VectorLayer. #js {:source (make-cluster-source)
                                          :style  (partial get-cluster-style false)}))

(def map-view-atom (atom nil))

(def map-object-atom (atom nil))

(defn fit-map [^js src ^js view ^js map-obj]
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

(defn selected-ids-to-state [^js event]
  (->> (.getArray (.-target event))
       (map #(js->clj (.get % "features")))
       (flatten)
       (map #(.get % "file-id"))
       (set)
       (reset! state/map-selected-result-ids)))

(def selected-features (doto (Collection.)
                         (.on "add" selected-ids-to-state)
                         (.on "remove" selected-ids-to-state)))

(add-watch
  state/map-selected-result-ids
  :map-filter-reset
  (fn [_ _ _ nv]
    (when (and (zero? (count nv)) (> (.getLength selected-features) 0))
      (.clear selected-features))))

(defn remove-duplicate-consecutive-coords [coords]
  (reduce
    (fn [acc coord-pair]
      (if (= (last acc) coord-pair)
        acc
        (concat acc [coord-pair])))
    []
    coords))

(defn ol-map []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [map-layer (TileLayer. #js {:source (WMTS.
                                                      #js {:url             (proxy-url "wmts/maasto")
                                                           :attributions    #js [(Attribution. #js {:html "MML"})]
                                                           :matrixSet       "ETRS-TM35FIN"
                                                           :style           "default"
                                                           :projection      "EPSG:3067"
                                                           :format          "image/png"
                                                           :tileGrid        (make-tilegrid)
                                                           :layer           "taustakartta"
                                                           :requestEncoding "KVP"})})
             map-layer-kiinteisto (TileLayer. #js {:source        (WMTS.
                                                                        #js {:url             (proxy-url "wmts/kiinteisto")
                                                                             :attributions    #js [(Attribution. #js {:html "MML"})] :matrixSet "ETRS-TM35FIN"
                                                                             :style           "default"
                                                                             :projection      "EPSG:3067"
                                                                             :format          "image/png"
                                                                             :tileGrid        (make-tilegrid)
                                                                             :layer           "kiinteistojaotus"
                                                                             :requestEncoding "KVP"})
                                                       :maxResolution 4})

             drawing-source (VectorSource. #js {:wrapX    false
                                                    :features #js [(Feature. #js {:geometry (Polygon. (clj->js (:coordinates @state/search-query)))})]})

             drawing-layer (VectorLayer. #js {:source drawing-source
                                                  :style  (Style. #js {:fill   (Fill. #js {:color "rgba(255, 255, 255, 0.2)"})
                                                                                :stroke (Stroke. #js {:color marker-color :width 4})
                                                                                :image  (Circle. #js {:radius 7 :fill (Fill. #js {:color marker-color})})})})
             features (doto (Collection.)
                        (.on "add" (fn [^js event]
                                     (let [feature ^js (.-element event)
                                           new-coords (-> feature
                                                          .getGeometry
                                                          .getCoordinates
                                                          js->clj
                                                          first
                                                          remove-duplicate-consecutive-coords)]
                                       (swap! state/search-query update-in [:coordinates] conj new-coords))
                                     (.preventDefault event))))

             drawing-interaction (Draw. #js {:source   drawing-source
                                                            :type     "Polygon"
                                                            :features features
                                                            ; This checks that the drawing tool does not prevent clicking
                                                            ; on a search result (cluster) marker
                                                            :condition #(not (.hasFeatureAtPixel ^js @map-object-atom
                                                                                                 (.-pixel %)
                                                                                                 #js {:layerFilter (fn [layer-candidate]
                                                                                                                     (= layer-candidate cluster-layer))}))})

             select-interaction (Select. #js {:multi true
                                                             :layers #js [cluster-layer]
                                                             :style (partial get-cluster-style true)
                                                             :features selected-features})

             interactions (Collection. #js [(DragPan.) (MouseWheelZoom.) select-interaction drawing-interaction])

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
             cluster-source (make-cluster-source)]
         (.setSource cluster-layer cluster-source)
         (reset! map-view-atom (View. #js {:center     (clj->js (map-center))
                                              :minZoom    2
                                              :maxZoom    14
                                              :zoom       8
                                              :projection projektio}))
         (reset! map-object-atom (Map. #js {:controls     (-> (ol.control/defaults) (.extend (clj->js [(Control. #js {"element" custom-buttons})])))
                                               :target       "map"
                                               :view         @map-view-atom
                                               :layers       #js [map-layer map-layer-kiinteisto drawing-layer cluster-layer]
                                               :interactions interactions}))

         (doto @map-object-atom
           (.on "singleclick" (fn [event]
                                (when-not (or @drawing-enabled?
                                              (.hasFeatureAtPixel ^js @map-object-atom (.-pixel event) (fn [layer-candidate] (= layer-candidate cluster-layer))))
                                  (map-click event))))
           (.on "pointermove" (fn [event]
                                (let [pixel (.-pixel event)
                                      hit (.hasFeatureAtPixel ^js @map-object-atom pixel (fn [layer-candidate] (= layer-candidate cluster-layer)))
                                      target (.getElementById js/document (.getTarget ^js @map-object-atom))]
                                  (set! (-> target .-style .-cursor) (if hit "pointer" ""))))))

         (fit-map cluster-source @map-view-atom @map-object-atom)))

     :reagent-render
     (fn [this]
       [:div.map {:id "map"}])
     :component-did-update update-map-features}))
