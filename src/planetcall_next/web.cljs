(ns planetcall-next.web
  (:require
   ["phaser" :as Phaser]
   ["phaser3-rex-plugins/plugins/board-plugin.js" :as BoardPlugin]
   [clojure.string :as string]
   [planetcall-next.rules.games :as games]
   [planetcall-next.rules.units :as units]
   [planetcall-next.web.board :as rex]
   [planetcall-next.web.camera :refer [draggable-camera]]
   [planetcall-next.web.colors :as colors]
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
   colors/ORANGE])

(defn draw-space [scene board r [x y] space units]
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
            circle (new js/Phaser.Geom.Circle. x y (/ r 6))]
        (.fillCircleShape feature-gfx circle)
        (.strokeCircleShape feature-gfx circle)))
    (when-let [[color char] (improvement->color+char improvement)]
      (let [accent (nth PLAYER-COLORS controller)
            improvement-gfx (add-gfx scene {:fillStyle {:color color :alpha 0.5}
                                            :lineStyle {:color accent :alpha 0.5}})
            [x y] (midpoint center n-vertex nw-vertex)
            circle (new js/Phaser.Geom.Circle. x y (/ r 6))
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
      (let [road-gfx (add-gfx scene {:lineStyle {:color colors/BROWN :alpha 0.5
                                                 :width 3}})
            vert-road (rex/js-points [s-vertex nw-vertex])
            horz-road (rex/js-points [sw-vertex ne-vertex])]
        (.strokePoints road-gfx vert-road true)
        (.strokePoints road-gfx horz-road true)))
    (when miasma
      (let [miasma-gfx (add-gfx scene {:fillStyle {:color colors/CYAN :alpha 0.5}})
            points (rex/js-points [sw-vertex s-vertex (midpoint s-vertex center) (midpoint nw-vertex sw-vertex)])]
        (.fillPoints miasma-gfx points true)))
    (let [line-gfx (add-gfx scene {:lineStyle {:color colors/WHITE :alpha 0.5}})]
      (.strokePoints line-gfx points true))
    (when controller
      (let [color (nth PLAYER-COLORS controller)
            controller-gfx (add-gfx scene {:fillStyle {:color color :alpha 0.25}})
            points (rex/js-points [s-vertex se-vertex ne-vertex center])]
        (.fillPoints controller-gfx points)))
    (when (seq units)
      (doall
       (for [i (range (count units))
             :let [unit (nth units i)]]
         (let [col-width (/ (js/Math.abs (- (first center) (first ne-vertex))) 10)
               row-height (/ (js/Math.abs (- (second center) (second ne-vertex))) 10)
               color (nth PLAYER-COLORS (:faction unit))
               unit-gfx (add-gfx scene {:fillStyle {:color color :alpha 1}})
               points
               (rex/js-points
                [[(+ (first center)
                     col-width
                     (* 3 col-width i))
                  (- (second center)
                     (* 3 row-height i))]
                 [(+ (first center)
                     col-width
                     (* 3 col-width i))
                  (- (second s-vertex)
                     (* 2 row-height)
                     (* 3 row-height i))]
                 [(+ (first center)
                     (* 3 col-width)
                     (* 3 col-width i))
                  (- (second s-vertex)
                     (* 4 row-height)
                     (* 3 row-height i))]
                 [(+ (first center)
                     (* 3 col-width)
                     (* 3 col-width i))
                  (- (second center)
                     (* 2 row-height)
                     (* 3 row-height i))]])
               #_(rex/js-points
                [[(+ (first center) col-width) (second center)]
                 [(+ (first center) col-width) (- (second s-vertex) (* 2 row-height))]
                 [(+ (first center) (* 3 col-width)) (- (second s-vertex) (* 4 row-height))]
                 [(+ (first center) (* 3 col-width)) (- (second center) (* 2 row-height))]])
               #_(rex/js-points
                  [[(+ (first center) col-width) (second center)]
                   [(+ (first center) (* 2 col-width)) (- row-height (second center))]
                   [(+ (first s-vertex) col-width) (- row-height (second s-vertex))]
                   [(+ (first s-vertex) (* 2 col-width)) (- (* 2 row-height) (second s-vertex))]])]
           (js/console.log col-width row-height points)
           (.fillPoints unit-gfx points)))))))

(defn draw-tooltip-bg [scene x y w h]
  (let [container (.add.container scene x y)
        tooltip-rect (.add.rectangle scene 0 0 w h colors/BLACK)
        [improvement-text
         feature-text
         bools-text
         space-name-text
         coord-text
         :as text-objects]
        (reduce
         (fn [text-objects s]
           (let [first-object (first text-objects)
                 text-object (.add.text scene w (+ (.-y first-object) (.-height first-object)) s)]
             (.setOrigin text-object 1 0)
             (cons text-object text-objects)))
         [(let [text-object (.add.text scene w 0 "")]
            (.setOrigin text-object 1 0)
            text-object)]
         [""
          ""
          ""
          ""])]
    (.setOrigin tooltip-rect 0)
    (.add container (clj->js (cons tooltip-rect text-objects)))
    {:container container
     :tooltip-rect tooltip-rect
     :coord coord-text
     :space-name space-name-text
     :bools bools-text
     :feature feature-text
     :improvement improvement-text}))

(defn create-ui-scene [scene]
  (let [{coord-text :coord
         space-name-text :space-name
         bools-text :bools
         feature-text :feature
         improvement-text :improvement} (draw-tooltip-bg scene (- WIDTH 200) (- HEIGHT 100) 200 100)
        main-scene (.scene.get scene "main")]
    (.events.on main-scene "tilemove"
                (fn [coord space]
                  (let [game (.registry.get scene "game")
                        space-name
                        (->> [(:prefix space) (:suffix space)]
                             (map name)
                             (string/join " "))
                        frm (->> (select-keys space [:fungus :miasma :road])
                                 (filter (comp true? second))
                                 (map (comp name first))
                                 (string/join ", "))
                        feature-name
                        (if-let [feature (:feature space)]
                          (name feature)
                          "")
                        improvement
                        (let [{:keys [improvement controller]} space]
                          (if (and improvement controller)
                            (let [faction-name (get-in @game [:factions controller :name])]
                              (string/join " " [faction-name improvement]))
                            ""))]
                    (.setText coord-text (string/join ", " coord))
                    (.setText space-name-text space-name)
                    (.setText bools-text frm)
                    (.setText feature-text feature-name)
                    (.setText improvement-text improvement)))
                scene)))

(defn claim-space [game faction coord]
  (-> game
      (update-in [:factions faction :claimed] into coord)
      (assoc-in [:spaces coord :controller] faction)))

(defn realize-unit [game unit]
  (assoc-in game [:units (:id unit)] unit))

(defn create-main-scene [scene]
  (let [radius 32
        {:keys [board coords]} (rex/gen-board scene radius :map :standard)
        _camera (draggable-camera scene (/ WIDTH 2) (/ HEIGHT 2) 1)
        game (atom (games/init-game coords 6))]
    (.registry.set scene "game" game)
    (let [warrior (units/create-unit 0 [7 7] {:chassis :infantry :loadout :oldworld-weapons})
          engineer (units/create-unit 0 [7 7] {:chassis :infantry :loadout :engineering})
          walker (units/create-unit 0 [7 7] {:chassis :walker :loadout :oldworld-weapons})]
      (swap! game claim-space 0 [7 7])
      (swap! game realize-unit warrior)
      (swap! game realize-unit engineer)
      (swap! game realize-unit walker))
    (let [coord->units (group-by :coord (vals (:units @game)))]
      (doall
       (for [coord coords
             :let [units (coord->units coord)
                   space (get-in @game [:spaces coord])]]
         (draw-space scene board radius coord space units))))
    (.setInteractive board)
    (.on board "tilemove"
         (fn [_pointer xy]
           (let [coord [(.-x xy) (.-y xy)]
                 space (get-in @game [:spaces coord])]
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
