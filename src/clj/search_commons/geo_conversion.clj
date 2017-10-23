(ns search-commons.geo-conversion
  (:import [org.geotools.referencing.crs DefaultGeographicCRS]
           [org.geotools.referencing CRS]
           [org.geotools.geometry GeneralDirectPosition]))

(defn- round-to [n acc]
  (.setScale n acc BigDecimal/ROUND_HALF_UP))

(defn convert [source-CRS to-CRS coord-array]
  (let [math-transform (CRS/findMathTransform source-CRS to-CRS true)
        direct-pos (->> coord-array
                        (map (comp #(.doubleValue %) bigdec))
                        (into-array Double/TYPE)
                        GeneralDirectPosition.)
        result-point  (. math-transform transform direct-pos nil)]
    (->> result-point
         .getCoordinate
         (map (comp #(.doubleValue %) #(round-to % 6) bigdec)))))

(defn epsg3067->wgs84 [coord-array]
  (convert (CRS/decode "epsg:3067") DefaultGeographicCRS/WGS84 coord-array))

(defn wgs84->epsg3067 [coord-array]
  (convert DefaultGeographicCRS/WGS84 (CRS/decode "epsg:3067") coord-array))
