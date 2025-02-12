(ns planetcall-next.pixi.map
  (:require
   ["pixi.js" :refer [Container Graphics]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.utils :refer [midpoint]]
   [planetcall.geometry :as geo]))

(def sqrt3 (js/Math.sqrt 3))

(defn radius->points [r]
  (let [w (* r sqrt3)
        h (* r 2)]
    (map (partial map #(js/Math.round %))
         [[(* w 0.5) 0]
          [w (* h 0.25)]
          [w (* h 0.75)]
          [(* w 0.5) h]
          [0 (* h 0.75)]
          [0 (* h 0.25)]])))

(defn axial->offset [[q r]]
  [(+ q (/ (- r (mod r 2)) 2))
   r])

(defn draw-space [radius coord]
  (let [[n-vertex
         ne-vertex
         se-vertex
         s-vertex
         sw-vertex
         nw-vertex
         :as vertices] (radius->points radius)
        center (apply midpoint vertices)
        container (new Container)
        border-gfx (new Graphics)
        prefix-gfx (new Graphics)
        suffix-gfx (new Graphics)
        fungus-gfx (new Graphics)
        miasma-gfx (new Graphics)
        road-gfx (new Graphics)
        feature-gfx (new Graphics)
        w (* radius sqrt3)
        h (* radius 2)
        [x y] (axial->offset coord)
        px (cond-> (* x w)
             (odd? y) (+ (/ w 2))
             :also int)
        py (* y h 0.75)]
    (-> border-gfx
        (.poly (clj->js (flatten vertices)) true)
        (.stroke (clj->js {:color colors/WHITE
                           :width 2})))
    (-> prefix-gfx
        (.poly (clj->js (flatten [nw-vertex n-vertex center])))
        (.fill (clj->js {:color colors/GREEN})))
    (-> suffix-gfx
        (.poly (clj->js (flatten [ne-vertex n-vertex center])))
        (.fill (clj->js {:color colors/YELLOW})))
    (-> fungus-gfx
        (.poly (clj->js (flatten [nw-vertex
                                  center
                                  (midpoint s-vertex center)
                                  (midpoint nw-vertex sw-vertex)])))
        (.fill (clj->js {:color colors/RED})))
    (-> fungus-gfx
        (.poly (clj->js (flatten [s-vertex
                                  (midpoint s-vertex center)
                                  (midpoint nw-vertex sw-vertex)
                                  sw-vertex])))
        (.fill (clj->js {:color colors/TEAL})))
    (.addChild container
               prefix-gfx
               suffix-gfx
               fungus-gfx
               road-gfx
               miasma-gfx
               feature-gfx
               border-gfx)
    (pixi/move-to container [px py])
    container))

(defn create-map-view [radius n]
  (let [container (new Container)
        coords (geo/get-coords-within [0 0] n)]
    (doseq [coord coords
            :let [space-view (draw-space radius coord)]]
      (.addChild container space-view))
    container))
