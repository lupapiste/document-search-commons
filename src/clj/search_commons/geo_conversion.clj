(ns search-commons.geo-conversion
  (:import [java.math RoundingMode]
           [org.geotools.api.referencing.crs CoordinateReferenceSystem]
           [org.geotools.referencing.crs DefaultGeographicCRS]
           [org.geotools.referencing CRS]
           [org.geotools.geometry GeneralPosition]))

(defn- round-to [n acc]
  (.setScale n acc RoundingMode/HALF_UP))

(defn convert [^CoordinateReferenceSystem source-CRS ^CoordinateReferenceSystem to-CRS coord-array]
  (let [math-transform (CRS/findMathTransform source-CRS to-CRS true)
        direct-pos     (->> coord-array
                            ^doubles (into-array Double/TYPE)
                            GeneralPosition.)
        result-point   (.transform math-transform direct-pos nil)]
    (->> result-point
         .getCoordinate
         (map (comp #(.doubleValue %) #(round-to % 6) bigdec)))))

(defn epsg3067->wgs84 [coord-array]
  (convert (CRS/decode "epsg:3067") DefaultGeographicCRS/WGS84 coord-array))

(defn wgs84->epsg3067 [coord-array]
  (convert DefaultGeographicCRS/WGS84 (CRS/decode "epsg:3067") coord-array))
