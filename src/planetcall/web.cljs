(ns planetcall.web
  (:require
   ["phaser" :as Phaser]
   ["phaser3-rex-plugins/plugins/board-plugin.js" :as BoardPlugin]
   [clojure.string :as string]
   [planetcall.web.camera :refer [draggable-camera]]
   [planetcall.web.colors :as colors]
   [planetcall.web.rexboard :as rex]
   [shadow.cljs.modern :refer [defclass]]))

(set! *warn-on-infer* false)

(def HEIGHT 800)
(def WIDTH 800)

(defn midpoint [& coords]
  (let [xs (map first coords)
        ys (map second coords)]
    [(js/Math.round (/ (reduce + 0 xs) (count xs)))
     (js/Math.round (/ (reduce + ys) (count ys)))]))

(defn add-gfx [scene opts]
  (.add.graphics scene (clj->js opts)))

(def prefix->color
  {:rotting colors/GREEN
   :volatile colors/YELLOW
   :shattered colors/RED
   :preserved colors/PURPLE
   :ashen colors/WHITE})

(def suffix->color
  {:mountain colors/SILVER
   :canyon colors/DIM-GRAY
   :mesa colors/SALMON
   :steppe colors/YELLOW
   :marsh colors/LIME-GREEN
   :ooze colors/TEAL
   :wreckage colors/CRIMSON
   :wastes colors/LIGHT-SALMON})

(def feature->color+accent
  {:vents [colors/DARK-ORANGE colors/WHITE]
   :ruins [colors/SILVER-PINK colors/WHITE]
   :titan [colors/ROMAN-SILVER colors/WHITE]
   :xenobog [colors/SCREAMIN-GREEN colors/WHITE]})

(def improvement->color+char
  {:farm [colors/LIME-GREEN \F]
   :factory [colors/BROWN \M]
   :turbine [colors/YELLOW \E]
   :labs [colors/WHITE \L]})

(def PLAYER-COLORS
  [colors/RED
   colors/BLUE
   colors/GREEN
   colors/YELLOW
   colors/PURPLE
   colors/CYAN])

(defn gen-space
  ([coord]
   (gen-space coord {}))
  ([[x y]
    {:keys [miasma fungus road prefix suffix feature improvement controller]
     :or {miasma (rand-nth [true false])
          fungus (rand-nth [true false])
          road (rand-nth [true false])
          prefix (rand-nth (keys prefix->color))
          suffix (rand-nth (keys suffix->color))
          feature (rand-nth (cons nil (keys feature->color+accent)))
          improvement (rand-nth (cons nil (keys improvement->color+char)))
          controller (rand-int 6)}}]
   {:coords [x y]
    :miasma miasma
    :fungus fungus
    :road road
    :prefix prefix
    :suffix suffix
    :feature feature
    :improvement improvement
    :controller controller}))

(defn draw-space [scene board r [x y] space]
  (let [{:keys [prefix suffix feature miasma road fungus improvement controller]} space
        points (.getGridPoints board x y true)
        [ne-vertex
         se-vertex
         s-vertex
         sw-vertex
         nw-vertex
         n-vertex] (rex/cljs-points points)
        center (midpoint s-vertex n-vertex)]
    (let [prefix-gfx (add-gfx scene {:fillStyle {:color (prefix->color prefix) :alpha 0.5}})]
      (.fillPoints prefix-gfx (rex/js-points [center nw-vertex n-vertex]) true))
    (let [suffix-gfx (add-gfx scene {:fillStyle {:color (suffix->color suffix) :alpha 0.5}})]
      (.fillPoints suffix-gfx (rex/js-points [center ne-vertex n-vertex]) true))
    (when-let [[color accent] (feature->color+accent feature)]
      (let [feature-gfx (add-gfx scene {:fillStyle {:color color :alpha 0.5}
                                        :lineStyle {:color accent :alpha 0.5}})
            [x y] (midpoint center n-vertex ne-vertex)
            circle (new Phaser.Geom.Circle. x y (/ r 6))]
        (.fillCircleShape feature-gfx circle)
        (.strokeCircleShape feature-gfx circle)))
    (when-let [[color char] (improvement->color+char improvement)]
      (let [accent (nth PLAYER-COLORS controller)
            improvement-gfx (add-gfx scene {:fillStyle {:color color :alpha 0.5}
                                            :lineStyle {:color accent :alpha 0.5}})
            [x y] (midpoint center n-vertex nw-vertex)
            circle (new Phaser.Geom.Circle. x y (/ r 6))
            text-opts
            (clj->js
             {:fontSize "10px"
              :color "#000"})]
        (.fillCircleShape improvement-gfx circle)
        (.strokeCircleShape improvement-gfx circle)
        (.setOrigin (.add.text scene x y char text-opts) 0.5)))
    (when fungus
      (let [fungus-gfx (add-gfx scene {:fillStyle {:color colors/RED :alpha 0.5}})
            points (rex/js-points [nw-vertex (midpoint nw-vertex sw-vertex) (midpoint s-vertex center) center])]
        (.fillPoints fungus-gfx points true)))
    (when road
      (let [road-gfx (add-gfx scene {:lineStyle {:color colors/BROWN :alpha 0.5}})
            vert-road (rex/js-points [s-vertex nw-vertex])
            horz-road (rex/js-points [sw-vertex ne-vertex])]
        (.strokePoints road-gfx vert-road true)
        (.strokePoints road-gfx horz-road true)))
    (when miasma
      (let [miasma-gfx (add-gfx scene {:fillStyle {:color colors/CYAN :alpha 0.5}})
            points (rex/js-points [sw-vertex s-vertex (midpoint s-vertex center) (midpoint nw-vertex sw-vertex)])]
        (.fillPoints miasma-gfx points true)))
    (let [line-gfx (add-gfx scene {:lineStyle {:color colors/WHITE :alpha 0.5}})]
      (.strokePoints line-gfx points true))))

(defn draw-tooltip-bg [scene x y w h]
  (let [container (.add.container scene x y)
        tooltip-rect (.add.rectangle scene 0 0 w h colors/BLACK)
        coord-text (.add.text scene w 0 "x, y")
        space-name-text (.add.text scene w (+ (.-y coord-text) (.-height coord-text)) "buggy dunes")
        bools-text (.add.text scene w (+ (.-y space-name-text) (.-height space-name-text)) "fungus, miasma, road")]
    (.setOrigin coord-text 1 0)
    (.setOrigin space-name-text 1 0)
    (.setOrigin bools-text 1 0)
    (.setOrigin tooltip-rect 0)
    (.add container (clj->js
                     [tooltip-rect coord-text space-name-text bools-text]))
    {:container container
     :tooltip-rect tooltip-rect
     :coord coord-text
     :space-name space-name-text
     :bools bools-text}))

(defn create-ui-scene [scene]
  (let [{coord-text :coord
         space-name-text :space-name
         bools-text :bools} (draw-tooltip-bg scene (- WIDTH 200) (- HEIGHT 100) 200 100)
        main-scene (.scene.get scene "main")]
    (.events.on main-scene "tilemove"
                (fn [coord space]
                  (let [space-name
                        (->> [(:prefix space) (:suffix space)]
                             (map name)
                             (string/join " "))
                        frm (->> (select-keys space [:fungus :miasma :road])
                                 (filter (comp true? second))
                                 (map (comp name first))
                                 (string/join ", "))]
                    (.setText coord-text (string/join ", " coord))
                    (.setText space-name-text space-name)
                    (.setText bools-text frm)))
                scene)))

(defn create-main-scene [scene]
  (let [radius 32
        {:keys [board coords]} (rex/gen-board scene radius :map :standard)
        _camera (draggable-camera scene (/ WIDTH 2) (/ HEIGHT 2) 1)
        spaces (reduce
                (fn [spaces coord]
                  (let [space (gen-space coord)]
                    (assoc spaces coord space)))
                {}
                coords)]
    (doall
     (for [coord coords
           :let [space (get spaces coord)]]
       (draw-space scene board radius coord space)))
    (.setInteractive board)
    (.on board "tilemove"
         (fn [_pointer xy]
           (let [coord [(.-x xy) (.-y xy)]
                 space (get spaces coord)]
             (when space
               (.events.emit scene "tilemove" coord space)))))))

(defclass UIScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "ui" :active true})))
  Object
  (create [this] (create-ui-scene this)))

(defclass MainScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "main" :active true})))
  Object
  (create [this] (create-main-scene this)))

(def config (clj->js {:type (.-AUTO Phaser)
                      :scene [MainScene UIScene]
                      :width WIDTH
                      :height HEIGHT
                      :parent "app"
                      :scale {:mode (-> Phaser .-Scale .-FIT)
                              :autoCenter (-> Phaser .-Scale .-CENTER_BOTH)}
                      :plugins {:scene [{:key "rexBoard"
                                         :plugin BoardPlugin/default
                                         :mapping "rexBoard"}]}}))

(defonce game (atom nil))

(defn main []
  (if @game
    (js/window.location.reload)
    (reset! game (new (.-Game Phaser) config))))

(main)
