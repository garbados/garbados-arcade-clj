(ns planetcall-next.web.board 
  (:require
   [planetcall-next.rules.scenarios :as scenarios]))

(set! *warn-on-infer* false)

(defn init-board [scene w h x y size]
  (.rexBoard.add.board scene (clj->js {:grid {:gridType "hexagonGrid"
                                              :x x
                                              :y y
                                              :size size
                                              :staggeraxis "x"
                                              :staggerindex "even"}
                                       :width w
                                       :height h})))

(defn fit-board-parallelogram [scene board n]
  (.fit board (.rexBoard.hexagonMap.parallelogram scene board 2 n n)))

(defn fit-board-triangle [scene board n]
  (.fit board (.rexBoard.hexagonMap.triangle scene board 0 n)))

(defn fit-board-hexagon [scene board n]
  (.fit board (.rexBoard.hexagonMap.hexagon scene board n)))

(def shape->fit-map
  {:parallelogram fit-board-parallelogram
   :triangle fit-board-triangle
   :hexagon fit-board-hexagon})

(defn gen-board [scene size & {:keys [x y w h scenario]
                               :or {x 0
                                    y 0
                                    w 0
                                    h 0
                                    scenario :standard}}]
  (let [{shape :shape n :size} (scenarios/scenario->details scenario)
        fit-board (shape->fit-map shape)
        board (init-board scene w h x y size)
        points (fit-board scene board n)]
    {:board board
     :coords
     (set
      (for [point points
            :let [x (.-x point) y (.-y point)]]
        [x y]))}))

(defn cljs-points [points]
  (map (fn [p] [(.-x p) (.-y p)]) points))

(defn js-points [points]
  (clj->js (map (fn [[x y]] (clj->js {:x x :y y})) points)))
