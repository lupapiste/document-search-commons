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
            [ol.layer.Tile]
            [ol.layer.Vector]
            [ol.proj]
            [ol.source.Vector]
            [ol.source.WMTS]
            [ol.style.Circle]
            [ol.style.Fill]
            [ol.style.Stroke]
            [ol.style.Style]
            [ol.tilegrid.WMTS]
            [ajax.core :refer [GET]]
            [clojure.string :as s]
            [reagent.core :as reagent]
            [search-commons.utils.i18n :refer [t]]
            [search-commons.utils.state :as state]))

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
  (when (= (-> event .-map .getInteractions .getLength) 2)
    (get-property-id (-> event .-coordinate js->clj))))

(defn ol-map []
  (reagent/create-class
    {:component-did-mount
     (fn [this]
       (let [map-layer (ol.layer.Tile. #js {:source (ol.source.WMTS.
                                                      #js {:url (proxy-url "wmts/maasto")
                                                           :attributions #js [(ol.Attribution. #js {:html "MML"})]
                                                           :matrixSet "ETRS-TM35FIN"
                                                           :style "default"
                                                           :projection "EPSG:3067"
                                                           :format "image/png"
                                                           :tileGrid (make-tilegrid)
                                                           :layer "taustakartta"
                                                           :requestEncoding "KVP"})})
             map-layer-kiinteisto (ol.layer.Tile. #js {:source (ol.source.WMTS.
                                                                 #js {:url (proxy-url "wmts/kiinteisto")
                                                                      :attributions #js [(ol.Attribution. #js {:html "MML"})] :matrixSet "ETRS-TM35FIN"
                                                                      :style "default"
                                                                      :projection "EPSG:3067"
                                                                      :format "image/png"
                                                                      :tileGrid (make-tilegrid)
                                                                      :layer "kiinteistojaotus"
                                                                      :requestEncoding "KVP"})
                                                       :maxResolution 4
                                                       })
             ;features (ol.Collection. #js [(ol.Feature. #js {:geometry (ol.geom.Polygon.  (clj->js [[[370000 6685600] [375999 6685600] [375999 6689800] [370000 6689800] [370000 6685600]]]))})])

             drawing-source (ol.source.Vector. #js {:wrapX false
                                                    :features #js [(ol.Feature. #js {:geometry (ol.geom.Polygon. (clj->js (:coordinates @state/search-query)))})]})

             drawing-layer (ol.layer.Vector. #js {:source drawing-source
                                                  :style (ol.style.Style. #js {:fill (ol.style.Fill. #js {:color "rgba(255, 255, 255, 0.2)"})
                                                                               :stroke (ol.style.Stroke. #js {:color "#ffcc33" :width 2})
                                                                               :image (ol.style.Circle. #js {:radius 7 :fill (ol.style.Fill. #js {:color "#ffcc33"})})})})
             features (doto (ol.Collection.)
                        (.on "add" (fn [event]
                                     (swap! state/search-query update-in [:coordinates] conj (-> event
                                                                                                 .-element
                                                                                                 .getGeometry
                                                                                                 .getCoordinates
                                                                                                 js->clj
                                                                                                 first))
                                     (.preventDefault event))))
             drawing-interaction (ol.interaction.Draw. #js {:source drawing-source
                                                            :type "Polygon"
                                                            :features features
                                                            })

             interactions (ol.Collection. #js [(ol.interaction.DragPan.) (ol.interaction.MouseWheelZoom.) drawing-interaction])

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
                              (.addEventListener button "click"
                                                 (fn [evt]
                                                   (let [class-now (-> evt (.-currentTarget) (.-className))
                                                         new-class (if (= class-now "icon-flow-line") "icon-location" "icon-flow-line")
                                                         new-title (if (= class-now "icon-flow-line") (t "Poimi kiinteistörekisterinumero") (t "Rajaa hakualue"))
                                                         ]
                                                     (.clear drawing-source)
                                                     (swap! state/search-query assoc :coordinates nil)
                                                     (if (= new-class "icon-flow-line")
                                                       (.push interactions drawing-interaction)
                                                       (.pop interactions))
                                                     (set! (.-className button) new-class)
                                                     (set! (.-title button) new-title)
                                                     (.preventDefault evt))))
                              div)]

         (doto
           (ol/Map. #js {:controls (-> (ol.control.defaults) (.extend (clj->js [(ol.control.Control. #js {"element" custom-buttons})])))
                         :target "map"
                         :view (ol.View. #js {:center (clj->js (map-center))
                                              :zoom 8
                                              :projection projektio})
                         :layers #js [map-layer drawing-layer map-layer-kiinteisto]
                         :interactions interactions})
           (.on "singleclick" map-click))))

     :reagent-render
     (fn [this]
       [:div.map {:id "map"}])}))
