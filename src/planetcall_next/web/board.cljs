(ns planetcall-next.web.board)

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

(defn fit-board-duel [scene board n]
  (.fit board (.rexBoard.hexagonMap.parallelogram scene board 2 n n)))

(defn fit-board-triad-trial [scene board n]
  (.fit board (.rexBoard.hexagonMap.triangle scene board 0 n)))

(defn fit-board-standard [scene board n]
  (.fit board (.rexBoard.hexagonMap.hexagon scene board n)))

(def map-data
  {:duel [fit-board-duel 24]
   :triad [fit-board-triad-trial 18]
   :standard [fit-board-standard 12]})

(defn gen-board [scene size & {:keys [x y w h]
                               map-size :map
                               :or {x 0
                                    y 0
                                    w 0
                                    h 0
                                    map-size :standard}}]
  (let [[map-fn n] (get map-data map-size)
        board (init-board scene w h x y size)
        points (map-fn scene board n)]
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
