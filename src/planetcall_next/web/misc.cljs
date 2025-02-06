(ns planetcall-next.web.misc
  "Misc that I didn't want to throw away.")

(comment
  (defn cool-points [l [x y]]
    [x (- y l)
     (+ x l) (+ y (/ l 2))
     (+ x l) (- y (/ l 2))
     x (+ y l)
     (- x l) (- y (/ l 2))
     (- x l) (+ y (/ l 2))])
  

  (defn flat-top-points [l [x y]]
    [(+ x (/ l 2)) (- y l)
     (- x (/ l 2)) (- y l)
     (- x l) y
     (- x (/ l 2)) (+ y l)
     (+ x (/ l 2)) (+ y l)
     (+ x l) y])
  

  (defn flat-top-origin [l k j]
    [(* l k)
     (cond-> (* l j)
       (odd? k) (+ l (/ l 2)))])
  

  #_
    (defn add+draw-hexagon-in-mesh [scene l origin & {:keys [color]}]
      (add+draw-hexagon scene origin l :color color))
  

  (defn add-gradients-to-hexagon [scene j k hexagon]
    (let [[x y] [(.-x hexagon) (.-y hexagon)]
          l (int (/ (.-width hexagon) 2))
          mask (.createGeometryMask hexagon)
          [fx-x fx-y] [(cond-> (+ x (* l (dec j) 1.5) (* -0.5 l))
                         (odd? k) (+ (* l 0.75))
                         :finally (int))
                       (int (+ y (* (dec k) l 0.5)))]
          fx (-> scene .-add (.image fx-x fx-y "bg") (.setOrigin 0.25))]
      (set! (.-mask fx) mask)
      (-> fx .-preFX (.addGradient YELLOW WHITE 0.3 (/ 1 6) 0 0.5 0.5 3))
      (-> fx .-preFX (.addGradient RED WHITE 1 (/ 1 6) (/ 5 6) 0.5 0.5 3))
      (.setDisplaySize fx (.-width hexagon) (.-height hexagon))
      fx))
  

  #_
    (defn add+draw-hexagon-mesh
      [scene l r n
       & {:keys [color stroke-color stroke-width]
          :or {color BLACK
               stroke-color WHITE
               stroke-width 2}}]
      (doall
       (for [i (range n)
             :let [j (inc (mod i r))
                   k (inc (int (/ i r)))
                   [x y] (get-origin l j k)
                   {:keys [hexagon]} (add+draw-hexagon-extra scene [x y] l {} :color color)]]
         (do
           (.setStrokeStyle hexagon stroke-width stroke-color)
           hexagon))))
  

  (defn get-origin [l q r]
    (let [s (- 0 q r)]
      [(cond-> (* l q)
         (odd? s) (- (/ l 2)))
       (* l r 0.75)])
    #_[(* l k)
       (cond-> (* l j)
         (odd? k) (+ (/ l 2)))]
    #_[(cond-> k
         (odd? j) (inc)
         :lengthen (* l)
         (even? k) (- (/ l 2))
         :also (int))
       (cond-> j
         (odd? k) (inc)
         :lengthen (* l 1.5)
         (even? j) (- (/ l 2))
         :also (int))]
    #_[(* l k) (* l j)]
    #_[(cond-> (* l k)
         (odd? j) (+ (/ l 2))
         :finally (int))
       (cond-> (* l j)
         (odd? k) (- (/ l 2) l)
         :finally (int))])
  

  (defn axial-to-offset [[q r]]
    [r
     (+ q (/ (- r (bit-and r 1)) 2))])
  

  (defn hex-board [n]
    (map axial-to-offset
         (get-coords-within [0 0] n)))
  

  (defn add+draw-hexagon
    [scene [x y] l & {:keys [color] :or {color BLACK}}]
    (let [points [x (+ y l)
                  (+ x l) (+ y (/ l 2))
                  (+ x l) (- y (/ l 2))
                  x (- y l)
                  (- x l) (- y (/ l 2))
                  (- x l) (+ y (/ l 2))]
          hexagon (-> scene .-add (.polygon x y (clj->js points) color))]
      {:hexagon hexagon}))
  

  (defn add+draw-triangle
    [scene [x y] [x1 y1] [x2 y2] [x3 y3] & {:keys [color] :or {color BLACK}}]
    (-> scene .-add (.triangle x y x1 y1 x2 y2 x3 y3 color)))
  

  (defn add+draw-hexagon-extra
    [scene [x y] l space & {:keys [color] :or {color BLACK}}]
    (let [container (-> scene .-add (.container x y))
          [ne-vertex
           nw-vertex
           w-vertex
           sw-vertex
           se-vertex
           e-vertex
           :as vertices]
          [[(+ x (/ l 2)) (- y l)]
           [(- x (/ l 2)) (- y l)]
           [(- x l) y]
           [(- x (/ l 2)) (+ y l)]
           [(+ x (/ l 2)) (+ y l)]
           [(+ x l) y]]
          center (midpoint e-vertex w-vertex)
          hexagon (-> scene .-add (.polygon 0 0 (clj->js (flatten vertices)) color))
          rainfall (add+draw-triangle scene [0 0] center nw-vertex ne-vertex :color GREEN)
          terrain (add+draw-triangle scene [0 0] center nw-vertex w-vertex :color YELLOW)
          vegetation (add+draw-triangle scene [0 0] center sw-vertex w-vertex :color RED)
          miasma-rhombus (flatten [sw-vertex se-vertex (midpoint e-vertex se-vertex) (midpoint center sw-vertex)])
          miasma (-> scene .-add (.polygon (int (/ l 4)) (- (int (/ l 2))) (clj->js miasma-rhombus) TEAL))
          units
          (doall
           (for [i (range 3)
                 :let [dx 0
                       dy (- 0 (* i l (/ 3 12)) (/ l 2))
                       [left top right]
                       [[(- x (/ l 4)) y]
                        [x (- y (/ l 3))]
                        [(+ x (/ l 4)) y]]
                       triangle (add+draw-triangle scene [dx dy] left top right :color PURPLE)]]
             (do
               (.setOrigin triangle 1)
               (.setStrokeStyle triangle 1 BLUE)
               triangle)))]
      (.add container (clj->js [hexagon rainfall terrain vegetation miasma]))
      (.add container (clj->js units))
      (.setOrigin rainfall 1)
      (.setOrigin terrain 1)
      (.setOrigin vegetation 1)
      (.setOrigin miasma 1)
      {:container container
       :hexagon hexagon
       :rainfall rainfall
       :terrain terrain
       :vegetation vegetation
       :miasma miasma}))
  )
