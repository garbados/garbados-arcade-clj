(ns planetcall-next.web
  (:require
   ["phaser" :as Phaser]
   ["phaser3-rex-plugins/plugins/board-plugin.js" :as BoardPlugin]
   [clojure.string :as string]
   [planetcall-next.rules.scenarios :as scenarios]
   [planetcall-next.rules.spaces :as spaces]
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
  {:rotting :green
   :volatile :yellow
   :shattered :red
   :preserved :purple
   :ashen :gray})

(def suffix->color
  {:mountain :silver
   :canyon :dgray
   :mesa :salmon
   :steppe :yellow
   :marsh :lime
   :ooze :teal
   :wreckage :crimson
   :wastes :lsalmon})

(def feature->color+accent
  {:vents [:dorange :white]
   :ruins [:spink :white]
   :titan [:roman :white]
   :xenobog [:sgreen :white]})

(def improvement->color+char
  {:farm [:lime \F]
   :factory [:brown \M]
   :turbine [:yellow \E]
   :labs [:white \L]
   :stockpile [:silver \S]})

(def PLAYER-COLORS
  [:red
   :blue
   :yellow
   :green
   :purple
   :orange])

(def COLORS
  [[:black colors/BLACK]
   [:white colors/WHITE]
   [:brown colors/BROWN]
   [:gray colors/GRAY]
   [:red colors/RED]
   [:blue colors/BLUE]
   [:green colors/GREEN]
   [:yellow colors/YELLOW]
   [:purple colors/PURPLE]
   [:orange colors/ORANGE]
   [:lime colors/LIME-GREEN]
   [:sgreen colors/SCREAMIN-GREEN]
   [:roman colors/ROMAN-SILVER]
   [:spink colors/SILVER-PINK]
   [:dorange colors/DARK-ORANGE]
   [:silver colors/SILVER]
   [:dgray colors/DIM-GRAY]
   [:salmon colors/SALMON]
   [:teal colors/TEAL]
   [:cyan colors/CYAN]
   [:crimson colors/CRIMSON]
   [:lsalmon colors/LIGHT-SALMON]])

(defn init-gfx [scene & {:keys [colors]
                         :or {colors COLORS}}]
  {:tinted
   (reduce
    (fn [gfx [color-name color-code]]
      (->> {:fillStyle {:color color-code :alpha 0.25}
            :lineStyle {:color color-code :alpha 0.25}}
           (add-gfx scene)
           (assoc gfx color-name)))
    {}
    colors)
   :faded
   (reduce
    (fn [gfx [color-name color-code]]
      (->> {:fillStyle {:color color-code :alpha 0.5}
            :lineStyle {:color color-code :alpha 0.5}}
           (add-gfx scene)
           (assoc gfx color-name)))
    {}
    colors)
   :solid
   (reduce
    (fn [gfx [color-name color-code]]
      (->> {:fillStyle {:color color-code :alpha 1}
            :lineStyle {:color color-code :alpha 1}}
           (add-gfx scene)
           (assoc gfx color-name)))
    {}
    colors)
   :wide
   (reduce
    (fn [gfx [color-name color-code]]
      (->> {:lineStyle {:color color-code :alpha 0.5 :width 3}}
           (add-gfx scene)
           (assoc gfx color-name)))
    {}
    colors)})

(defn draw-units [scene gfx ne-vertex center s-vertex units]
  (doseq [i (range (count units))
          :let [unit (nth units i)]]
    (let [col-width (/ (js/Math.abs (- (first center) (first ne-vertex))) 10)
          row-height (/ (js/Math.abs (- (second center) (second ne-vertex))) 10)
          hp% (- 1 (/ (:integrity unit) (:max-integrity unit)))
          color (nth PLAYER-COLORS (:faction unit))
          unit-line-gfx (get-in gfx [:faded color])
          unit-fill-gfx (get-in gfx [:solid color])
          [nw-corner
           sw-corner
           se-corner
           ne-corner]
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
               (* 3 row-height i))]]
          unit-height (- (second nw-corner) (second sw-corner))
          hp-points
          (rex/js-points
           [[(first nw-corner)
             (- (second nw-corner)
                (* unit-height hp%))]
            sw-corner
            se-corner
            [(first ne-corner)
             (- (second ne-corner)
                (* unit-height hp%))]])
          points
          (rex/js-points
           [nw-corner
            sw-corner
            se-corner
            ne-corner])
          n-midpoint (midpoint nw-corner ne-corner)
          s-midpoint (midpoint sw-corner se-corner)
          unit-center (midpoint n-midpoint s-midpoint)
          unit-quarter (midpoint unit-center s-midpoint)
          text-opts (clj->js {:fontSize "10px" :color "#fff"})]
      (.fillPoints unit-fill-gfx hp-points true)
      (.strokePoints unit-line-gfx points true)
      (let [loadout-char (first (name (get-in unit [:design :loadout])))
            [n-x n-y] unit-center
            loadout-text (.add.text scene n-x n-y loadout-char text-opts)
            circle (.fillCircle (get-in gfx [:solid :black]) n-x n-y (* col-width))]
        (.children.moveUp scene circle)
        (.setOrigin loadout-text 0.5))
      (let [chassis-char (first (name (get-in unit [:design :chassis])))
            [s-x s-y] unit-quarter
            chassis-text (.add.text scene s-x s-y chassis-char text-opts)
            circle (.fillCircle (get-in gfx [:solid :black]) s-x s-y (* col-width))]
        (.children.moveUp scene circle)
        (.setOrigin chassis-text 0.5)))))

(defn draw-space [scene gfx board r [x y] space units]
  (let [{:keys [prefix suffix feature miasma road fungus improvement controller]} space
        points (.getGridPoints board x y true)
        [ne-vertex
         se-vertex
         s-vertex
         sw-vertex
         nw-vertex
         n-vertex
         :as vertices] (rex/cljs-points points)
        center (apply midpoint vertices)]
    (let [prefix-gfx (get-in gfx [:faded (prefix->color prefix)])]
      (.fillPoints prefix-gfx (rex/js-points [center nw-vertex n-vertex]) true))
    (let [suffix-gfx (get-in gfx [:faded (suffix->color suffix)])]
      (.fillPoints suffix-gfx (rex/js-points [center ne-vertex n-vertex]) true))
    (when-let [[color accent] (feature->color+accent feature)]
      (let [ft-line-gfx (get-in gfx [:wide accent])
            ft-fill-gfx (get-in gfx [:solid color])
            [x y] (midpoint center n-vertex ne-vertex)
            circle (new js/Phaser.Geom.Circle. x y (/ r 6))]
        (.fillCircleShape ft-fill-gfx circle)
        (.strokeCircleShape ft-line-gfx circle)))
    (when-let [[color char] (improvement->color+char improvement)]
      (let [accent (nth PLAYER-COLORS controller)
            imp-fill-gfx (get-in gfx [:solid color])
            imp-line-gfx (get-in gfx [:wide accent])
            [x y] (midpoint center n-vertex nw-vertex)
            circle (new js/Phaser.Geom.Circle. x y (/ r 6))
            text-opts
            (clj->js
             {:fontSize "10px"
              :color "#000"})]
        (.fillCircleShape imp-fill-gfx circle)
        (.strokeCircleShape imp-line-gfx circle)
        (.setOrigin (.add.text scene x y char text-opts) 0.5)))
    (when fungus
      (let [fungus-gfx (get-in gfx [:faded :red])
            points (rex/js-points [nw-vertex (midpoint nw-vertex sw-vertex) (midpoint s-vertex center) center])]
        (.fillPoints fungus-gfx points true)))
    (when road
      (let [road-gfx (get-in gfx [:wide :brown])
            vert-road (rex/js-points [s-vertex nw-vertex])
            horz-road (rex/js-points [sw-vertex ne-vertex])]
        (.strokePoints road-gfx vert-road true)
        (.strokePoints road-gfx horz-road true)))
    (when miasma
      (let [miasma-gfx (get-in gfx [:faded :cyan])
            points (rex/js-points [sw-vertex s-vertex (midpoint s-vertex center) (midpoint nw-vertex sw-vertex)])]
        (->> (.fillPoints miasma-gfx points true)
             (.children.moveUp scene))))
    (let [line-gfx (get-in gfx [:faded :white])]
      (.strokePoints line-gfx points true))
    (when controller
      (let [color (nth PLAYER-COLORS controller)
            controller-gfx (get-in gfx [:tinted color])
            points (rex/js-points [s-vertex se-vertex ne-vertex center])]
        (.fillPoints controller-gfx points)))
    (when (seq units)
      (draw-units scene gfx ne-vertex center s-vertex units))))

(defn draw-space-tooltip [scene gfx x y w h]
  (let [container (.add.container scene x y)
        tooltip-rect (.add.rectangle scene 0 0 w h colors/BLACK)
        [improvement-text
         bools-text
         yield-text
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
    (.children.moveUp
     scene
     (.strokeRectShape (get-in gfx [:solid :white]) tooltip-rect))
    (.add container (clj->js (cons tooltip-rect text-objects)))
    {:container container
     :tooltip-rect tooltip-rect
     :coord coord-text
     :space-name space-name-text
     :bools bools-text
     :improvement improvement-text
     :yield yield-text}))

(defn make-space-tooltip
  [scene & {:keys [WIDTH HEIGHT]
            :or {WIDTH WIDTH HEIGHT HEIGHT}}]
  (let [w (/ WIDTH 3) h (/ HEIGHT 8)
        x (- WIDTH w) y (- HEIGHT h)
        transforms
        {:coord
         (fn [_game {:keys [coord]}]
           (str "[" (string/join ", " coord) "]"))
         :space-name
         (fn [_game {:keys [prefix suffix feature]}]
           (let [base-name (->> [prefix suffix]
                                (map name)
                                (string/join " "))]
             (if feature
               (str base-name ", " (name feature))
               base-name)))
         :bools
         (fn [_game space]
           (->> (select-keys space [:fungus :miasma :road])
                (filter (comp true? second))
                (map (comp name first))
                (string/join ", ")))
         :improvement
         (fn [game space]
           (let [{:keys [improvement controller]} space]
             (if (and improvement controller)
               (let [faction-name (get-in @game [:factions controller :name])]
                 (string/join " " [faction-name (name improvement)]))
               "")))
         :yield
         (fn [_game space]
           (->> (spaces/space-yield space)
                (filter (comp pos-int? second))
                (map #(string/join " " [(string/capitalize (first (name (first %))))
                                        (second %)]))
                (string/join ", ")))}
        gfx
        {:solid
         {:white (add-gfx scene {:x x
                                 :y y
                                 :fillStyle {:color colors/WHITE :alpha 1}
                                 :lineStyle {:color colors/WHITE :alpha 1 :width 3}})}}
        tooltip (draw-space-tooltip scene gfx x y w h)]
    [(fn [space]
       (let [game (.registry.get scene "game")]
         (doseq [[field f] transforms]
           (.setText (get tooltip field) (f game space)))))
     (fn []
       (doseq [[field _] transforms]
         (.setText (get tooltip field) "")))]))

(defn create-ui-scene [scene]
  (let [main-scene (.scene.get scene "main")
        [update-space-tooltip
         reset-space-tooltip]
        (make-space-tooltip scene)]
    (.events.on main-scene "tilemove"
                update-space-tooltip
                scene)
    (.events.on main-scene "tilereset"
                reset-space-tooltip
                scene)))

(defn create-main-scene [scene]
  (let [radius 64
        {:keys [board coords]} (rex/gen-board scene radius :scenario :standard)
        _camera (draggable-camera scene WIDTH HEIGHT 0.5)
        gfx (init-gfx scene)
        game (atom (scenarios/init-game-from-scenario coords :standard))]
    (.registry.set scene "game" game)
    (let [coord->units (group-by :coord (vals (:units @game)))]
      (doall
       (for [coord coords
             :let [units (coord->units coord)
                   space (get-in @game [:spaces coord])]]
         (draw-space scene gfx board radius coord space units))))
    (.setInteractive board)
    (.on board "tilemove"
         (fn [_pointer xy]
           (let [coord [(.-x xy) (.-y xy)]
                 space (get-in @game [:spaces coord])]
             (if space
               (.events.emit scene "tilemove" space)
               (.events.emit scene "tilereset")))))))

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
