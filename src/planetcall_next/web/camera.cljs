(ns planetcall-next.web.camera)

(defn draggable-camera [scene x y z]
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
                    #_inc
                    (fn [z]
                      (if (<= 1 z)
                        (inc z)
                        (* z 2)))
                    #_dec
                    (fn [z]
                      (if (< 1 z)
                        (dec z)
                        (/ z 2)))))
           (swap! zoom (partial max 0.25))
           (swap! zoom (partial min 10))
           (.setZoom camera @zoom)))
    camera))
