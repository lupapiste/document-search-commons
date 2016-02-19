(ns search-commons.utils.utils)

;; Bezier based on https://github.com/arian/cubic-bezier

(defn curve [x1 x2 t]
  (let [v (- 1 t)]
    (+ (* 3 v v t x1)
       (* 3 v t t x2)
       (* t t t))))

(defn derivative-curve [x1 x2 t]
  (let [v (- 1 t)]
    (+ (* 3 (* 2 (- t 1) t (+ (* v v))) x1)
       (* 3 (- (+ (* t t t) (* 2 v t))) x2))))

(defn bezier [x1 y1 x2 y2 epsilon]
  (fn [t] (if-let [newton-result (loop [t2         t
                                        iterations 0]
                                   (let [x2' (- (curve x1 x2 t2) t)]
                                     (if (< (Math/abs x2') epsilon)
                                       (curve y1 y2 t2)
                                       (let [d2 (derivative-curve x1 x2 t2)]
                                         (when (and (>= (Math/abs d2) 1e-6) (< iterations 8))
                                           (recur (/ (- t2 x2') d2) (inc iterations)))))))]
            newton-result
            (cond
              (< t 0) (curve y1 y2 0)
              (> t 1) (curve y1 y2 1)
              :else (loop [t0 0
                           t1 1
                           t2 t]
                      (if (< t0 t1)
                        (let [x2' (curve x1 x2 t2)
                              t0' (if (> t x2') t2 t0)
                              t1' (if (<= t x2') t2 t1)]
                          (if (< (Math/abs (- x2' t)) epsilon)
                            (curve y1 y2 t2)
                            (recur t0' t1' (+ (* (- t1' t0') 0.5) t0'))))
                        (curve y1 y2 t2)))))))

(def is-old-ie?
  (let [agent (.-userAgent js/navigator)
        version (second (re-find #".*MSIE (\d+)\.0.*" agent))]
    (and version (< version 10))))

(defn setup-print-to-console!
  "Set *print-fn* to console.log"
  []
  (set! *print-newline* false)
  (set! *print-length* 42)
  (if is-old-ie?
    (do (set! *print-fn*
          (fn [& args]
            (doseq [item args]
              (.log js/console item))))
        (set! *print-err-fn*
          (fn [& args]
            (doseq [item args]
              (.error js/console item)))))
    (do (set! *print-fn*
          (fn [& args]
            (.apply (.-log js/console) js/console (into-array args))))
        (set! *print-err-fn*
          (fn [& args]
            (.apply (.-error js/console) js/console (into-array args))))))
  nil)
