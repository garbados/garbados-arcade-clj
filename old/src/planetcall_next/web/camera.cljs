(ns planetcall-next.web.camera)

(defn draggable-camera [scene x y z & {:keys [min-zoom
                                              max-zoom]
                                       :or {min-zoom 0.25
                                            max-zoom 3}}]
  (let [camera (-> scene .-cameras .-main)
        zoom (atom z)]
    (.setZoom camera @zoom)
    (.centerOn camera x y)
    (.on (.-input scene) "pointermove"
         (fn [p]
           (when (.-isDown p)
             (set! (.-scrollX camera) (+ (.-scrollX camera)
                                         (* (/ 1 @zoom)
                                            (- (-> p .-prevPosition .-x)
                                               (.-x p)))))
             (set! (.-scrollY camera) (+ (.-scrollY camera)
                                         (* (/ 1 @zoom)
                                            (- (-> p .-prevPosition .-y)
                                               (.-y p))))))))
    (.on (.-input scene) "wheel"
         (fn [p]
           (swap! zoom
                  (if (neg? (.-deltaY p))
                    (fn [z]
                      (if (<= 1 z)
                        (inc z)
                        (* z 2)))
                    (fn [z]
                      (if (< 1 z)
                        (dec z)
                        (/ z 2)))))
           (swap! zoom (partial max min-zoom))
           (swap! zoom (partial min max-zoom))
           (.setZoom camera @zoom)))
    camera))
