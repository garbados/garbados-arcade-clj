(ns planetcall-next.web.ideograph)

(def PI js/Math.PI)

(defn deg->rad [degrees]
  (* degrees (/ js/Math.pi 180)))

(defn polygon-points [[x y] sides r & {:keys [rotation]
                                        :or {rotation 0}}]
  (let [rotation* (mod rotation 360)
        angle (/ (* 2 PI) sides)
        rotation** (+ rotation* (/ angle 2))]
    (reduce
     (fn [points i]
       (let [angle (+ rotation** (* angle i))
             px (+ x (* r (js/Math.sin angle)))
             py (+ y (* r (js/Math.cos angle)))]
         (cons [px py] points)))
     []
     (range sides))))