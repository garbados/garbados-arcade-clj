(ns planetcall-next.web
  (:require
   ["phaser" :as Phaser]
   [planetcall-next.web.camera :as camera]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.config :as config]
   [planetcall-next.web.scenes.map :as map]
   [planetcall-next.web.utils :refer [midpoint]]
   [planetcall.geometry :as geo]
   [shadow.cljs.modern :refer [defclass]]))

(set! *warn-on-infer* false)

(def sqrt3 (js/Math.sqrt 3))

(defn size->points [size]
  (let [w (* size sqrt3)
        h (* size 2)]
    [[(* w 0.5) 0]
     [w (* h 0.25)]
     [w (* h 0.75)]
     [(* w 0.5) h]
     [0 (* h 0.75)]
     [0 (* h 0.25)]]))

(defn axial->offset [[q r]]
  [(+ q (/ (- r (mod r 2)) 2))
   r])

(defn create-hex-board [scene radius n & {:keys [width height]
                                          :or {width (/ config/WIDTH 2)
                                               height (/ config/HEIGHT 2)}}]
  (let [container (.add.container scene width height)
        coords (geo/get-coords-within [0 0] n)
        w (* radius sqrt3)
        h (* radius 2)
        points (size->points radius)
        js-points (clj->js (map (fn [[x y]] (clj->js {:x x :y y})) points))
        hexagons
        (reduce
         (fn [polygons [coord container object]]
           (assoc polygons coord {:container container
                                  :object object}))
         {}
         (for [[q r] coords
               :let [[x y] (axial->offset [q r])
                     px (cond-> (* x w)
                          (odd? y) (+ (/ w 2))
                          :also int)
                     py (* y h 0.75)
                     p-container (.add.container scene px py)
                     polygon (.add.polygon scene 0 0 js-points)]]
           (do
             (.add p-container polygon)
             (.add container p-container)
             [[x y] p-container polygon])))]
    {:container container
     :vertices points
     :center (apply midpoint points)
     :hexagons hexagons}))

(defn create-test-screen [scene]
  (let [width (/ config/WIDTH 2)
        height (/ config/HEIGHT 2)
        camera (camera/draggable-camera scene width height 0.5)
        radius 64
        [ct-x ct-y] [(- (/ radius 2))
                     (- (/ radius 2))]
        {:keys [hexagons center]
         [n-vertex
          ne-vertex
          se-vertex
          s-vertex
          sw-vertex
          nw-vertex
          :as vertices] :vertices}
        (create-hex-board scene radius 6 :width width :height height)]
    (doseq [{object :object
             container :container} (vals hexagons)]
      (let [[[x1 y1]
             [x2 y2]
             [x3 y3]] [nw-vertex n-vertex center]
            triangle (.add.triangle scene ct-x ct-y x1 y1 x2 y2 x3 y3 colors/WHITE)]
        (.add container triangle))
      (let [[[x1 y1]
             [x2 y2]
             [x3 y3]] [ne-vertex n-vertex center]
            triangle (.add.triangle scene ct-x ct-y x1 y1 x2 y2 x3 y3 colors/YELLOW)]
        (.add container triangle))
      (.setStrokeStyle object 3 colors/GREEN))))

(defclass TestScreen
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "title"})))
  Object
  (create [this] (create-test-screen this)))

(def game-config
  (clj->js {:type (.-AUTO Phaser)
            :scene [TestScreen]
            :width config/WIDTH
            :height config/HEIGHT
            :parent "app"
            :scale {:mode (-> Phaser .-Scale .-FIT)
                    :autoCenter (-> Phaser .-Scale .-CENTER_BOTH)}}))

(defonce game (atom nil))

(defn main []
  (if @game
    (js/window.location.reload)
    (reset! game (new (.-Game Phaser) game-config))))

(main)
