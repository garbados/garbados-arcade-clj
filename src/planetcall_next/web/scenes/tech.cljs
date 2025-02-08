(ns planetcall-next.web.scenes.tech 
  (:require
   [planetcall-next.rules.tech :refer [ideotech->details]]
   [planetcall-next.web.camera :as camera]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.geometry :as ideograph]
   [planetcall-next.rules.games :as games]))

(set! *warn-on-infer* false)

(defn lerp [p1 p2 t]
  (js/Phaser.Math.Linear p1 p2 t))

(def IDEO-NAMES [:military :industry :contact :ecology :science])
(def IDEO-COLORS [colors/RED colors/YELLOW colors/PURPLE colors/GREEN colors/SILVER])
(def SYNG-NAMES [:science-military :military-industry :industry-contact :contact-ecology :ecology-science])
(def SYNG-COLORS [colors/PINK colors/ORANGE colors/LIGHT-SALMON colors/CYAN colors/TEAL])

(defn mark-researched [all-circles ideology level n]
  (let [{circle :object}
        (get-in all-circles [ideology level n])]
    (.setStrokeStyle circle 5 colors/WHITE)))

(defn unmark-research [all-circles ideology level n]
  (let [{circle :object}
        (get-in all-circles [ideology level n])]
    (.setStrokeStyle circle 0 colors/BLACK)))

(defn draw-ideo-circles [scene x y]
  (let [main-ideo-circles
        (doall
         (flatten
          (for [i (range 4)
                :let [r (* (inc i) 128)
                      n (+ 3 i)
                      points (ideograph/polygon-points [x y] 5 r)
                      polygon (.add.polygon scene 0 0 (clj->js (flatten points)))]]
            (do
              (.setOrigin polygon 0)
              (for [j (range 5)
                    :let [[x1 y1] (nth points j)
                          [x2 y2]
                          (if (= j 4)
                            (first points)
                            (nth points (inc j)))
                          color (nth IDEO-COLORS j)]]
                (let [circle-objects
                      (for [k (range (dec n))
                            :let [[dx dy] [(lerp x1 x2 (/ (inc k) n))
                                           (lerp y1 y2 (/ (inc k) n))]]]
                        {:object (.add.circle scene dx dy 9 color)
                         :ideology (nth IDEO-NAMES j)
                         :level (inc i)
                         :n k})
                      circles (map :object circle-objects)
                      d1 (first circles)
                      d2 (last circles)
                      [[x1 y1] [x2 y2]] (map #(vec [(.-x %) (.-y %)]) [d1 d2])
                      tech-line (.add.line scene 0 0 x1 y1 x2 y2 colors/WHITE)]
                  (.setOrigin tech-line 0)
                  (.setLineWidth tech-line 3)
                  (.children.sendToBack scene tech-line)
                  circle-objects))))))
        syng-ideo-circles
        (doall
         (flatten
          (for [i (range 3)
                :let [r (+ 54 (* (inc i) 140))
                      n (+ 2 i)
                      points (ideograph/polygon-points [x y] 5 r :rotation 120)
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
                (let [circle-objects
                      (for [k (range (dec n))
                            :let [[dx dy] [(lerp x1 x2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))
                                           (lerp y1 y2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))]]]
                        {:object (.add.circle scene dx dy 9 color)
                         :ideology (nth SYNG-NAMES j)
                         :level (inc i)
                         :n k})
                      circles (map :object circle-objects)
                      d1 (first circles)
                      d2 (last circles)
                      [[x1 y1] [x2 y2]] (map #(vec [(.-x %) (.-y %)]) [d1 d2])
                      tech-line (.add.line scene 0 0 x1 y1 x2 y2 colors/WHITE)]
                  (.setOrigin tech-line 0)
                  (.setLineWidth tech-line 3)
                  (.children.sendToBack scene tech-line)
                  circle-objects))))))
        all-circles
        (reduce
         (fn [all-circles {:keys [ideology level n] :as ideo-circle}]
           (assoc-in all-circles [ideology level n] ideo-circle))
         {}
         (concat main-ideo-circles syng-ideo-circles))]
    all-circles))

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
  (let [x 0 y 0
        _camera (camera/draggable-camera scene x y 1)
        all-circles (draw-ideo-circles scene x y)
        {game :game} (.registry.get scene "game")
        player (mod (get-in @game [:turn :n]) (count (keys (:factions @game))))]
    (swap! game games/gain-tech-locator player :ecology 1 0)
    (let [faction (get-in @game [:factions player])
          known-tech (get-in faction [:research :known])]
      (doseq [{ideology :ideology
               level :level
               n :n} (map ideotech->details known-tech)]
        (mark-researched all-circles ideology level n)))))
