(ns planetcall-next.web.scenes.tech 
  (:require
   [planetcall-next.web.camera :as camera]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.ideograph :as ideograph]))

(set! *warn-on-infer* false)

(defn lerp [p1 p2 t]
  (js/Phaser.Math.Linear p1 p2 t))

(def TECH-COLORS [colors/RED colors/YELLOW colors/PURPLE colors/GREEN colors/SILVER])
(def SYNG-COLORS [colors/CRIMSON colors/ORANGE colors/LIGHT-SALMON colors/CYAN colors/TEAL])

(defn create-tech-scene
  "I'm creating a tech scene
   where every choice matters
   because everything passes through
   what you believe
   and belief is the only path
   to fulfilling survival.
   Belief in what?
   Oh child, that is up to you.
   Choice is the privilege of the living.
   Will you use it to thrive?"
  [scene]
  (let [_camera (camera/draggable-camera scene 0 0 1)
        circles
        (doall
         (flatten
          (for [i (range 4)
                :let [r (* (inc i) 128)
                      n (+ 3 i)
                      points (ideograph/polygon-points [0 0] 5 r)
                      polygon (.add.polygon scene 0 0 (clj->js (flatten points)))]]
            (do
              (.setOrigin polygon 0)
              ;; (.setStrokeStyle polygon 3 colors/WHITE)
              (for [j (range 5)
                    :let [[x1 y1] (nth points j)
                          [x2 y2]
                          (if (= j 4)
                            (first points)
                            (nth points (inc j)))
                          color (nth TECH-COLORS j)]]
                (let [circles
                      (for [k (range (dec n))
                            :let [[dx dy] [(lerp x1 x2 (/ (inc k) n))
                                           (lerp y1 y2 (/ (inc k) n))]]]
                        (.add.circle scene dx dy 9 color))
                      d1 (first circles)
                      d2 (last circles)
                      [[x1 y1] [x2 y2]] (map #(vec [(.-x %) (.-y %)]) [d1 d2])
                      tech-line (.add.line scene 0 0 x1 y1 x2 y2 colors/WHITE)]
                  (.setOrigin tech-line 0)
                  (.setLineWidth tech-line 3)
                  (.children.sendToBack scene tech-line)
                  circles))))))
        circles2
        (doall
         (flatten
          (for [i (range 3)
                :let [r (+ 48 (* (inc i) 142))
                      n (+ 2 i)
                      points (ideograph/polygon-points [0 0] 5 r :rotation 120)
                      polygon (.add.polygon scene 0 0 (clj->js (flatten points)))]]
            (do
              (.setOrigin polygon 0)
              ;; (.setStrokeStyle polygon 3 colors/WHITE)
              (for [j (range 5)
                    :let [[x1 y1] (nth points j)
                          [x2 y2]
                          (if (= j 4)
                            (first points)
                            (nth points (inc j)))
                          color (nth SYNG-COLORS j)
                          cramping 2]]
                (let [circles
                      (for [k (range (dec n))
                            :let [[dx dy] [(lerp x1 x2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))
                                           (lerp y1 y2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))]]]
                        (.add.circle scene dx dy 9 color))
                      d1 (first circles)
                      d2 (last circles)
                      [[x1 y1] [x2 y2]] (map #(vec [(.-x %) (.-y %)]) [d1 d2])
                      tech-line (.add.line scene 0 0 x1 y1 x2 y2 colors/WHITE)]
                  (.setOrigin tech-line 0)
                  (.setLineWidth tech-line 3)
                  (.children.sendToBack scene tech-line)
                  circles))))))]))
