(ns planetcall-next.web.scenes.map 
  (:require
   [planetcall-next.web.board :as rex]
   [planetcall-next.web.camera :as camera]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.config :as config]
   [planetcall-next.web.utils :refer [midpoint]]
   [planetcall.geometry :refer [get-adjacent-coords]]
   [dimdark.abilities :as a]))

(set! *warn-on-infer* false)

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

(defn draw-units [scene gfx ne-vertex center s-vertex units & {:keys [colors] :or {colors config/PLAYER-COLORS}}]
  (doseq [i (range (count units))
          :let [unit (nth units i)]]
    (let [col-width (/ (js/Math.abs (- (first center) (first ne-vertex))) 10)
          row-height (/ (js/Math.abs (- (second center) (second ne-vertex))) 10)
          hp% (- 1 (/ (:integrity unit) (:max-integrity unit)))
          color (nth colors (:faction unit))
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

(defn draw-space [scene gfx board r [x y] space units & {:keys [colors] :or {colors config/PLAYER-COLORS}}]
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
      (let [accent (nth colors controller)
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
      (let [color (nth colors controller)
            controller-gfx (get-in gfx [:tinted color])
            points (rex/js-points [s-vertex se-vertex ne-vertex center])]
        (.fillPoints controller-gfx points)))
    (when (seq units)
      (draw-units scene gfx ne-vertex center s-vertex units))))

(defn draw-adjacency-highlights [scene board r & {:keys [color width] :or {color colors/YELLOW width 7}}]
  (let [[sw-neighbor
         w-neighbor
         nw-neighbor
         se-neighbor
         e-neighbor
         ne-neighbor]
        (get-adjacent-coords [0 0])
        container (.add.container scene 0 0)
        [+x +y] [(* (/ r 4) (js/Math.sqrt 3)) (/ r 4)]
        [*x *y] [0 (/ r 2)]]
    (let [[x y] nw-neighbor
          points (.getGridPoints board x y true)
          [ne-vertex
           _se-vertex
           _s-vertex
           sw-vertex
           nw-vertex
           n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first nw-vertex) (second nw-vertex)
                            (first n-vertex) (second n-vertex))
                 (.add.line scene *x *y
                            (first nw-vertex) (second nw-vertex)
                            (first sw-vertex) (second sw-vertex))
                 (.add.line scene (* (/ r 4) (js/Math.sqrt 3)) (/ r 4)
                            (first ne-vertex) (second ne-vertex)
                            (first n-vertex) (second n-vertex))]]
      (.add container (clj->js lines)))
    (let [[x y] ne-neighbor
          points (.getGridPoints board x y true)
          [ne-vertex
           se-vertex
           _s-vertex
           _sw-vertex
           nw-vertex
           n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first nw-vertex) (second nw-vertex)
                            (first n-vertex) (second n-vertex))
                 (.add.line scene *x *y
                            (first ne-vertex) (second ne-vertex)
                            (first se-vertex) (second se-vertex))
                 (.add.line scene +x +y
                            (first ne-vertex) (second ne-vertex)
                            (first n-vertex) (second n-vertex))]]
      (.add container (clj->js lines)))
    (let [[x y] e-neighbor
          points (.getGridPoints board x y true)
          [ne-vertex
           se-vertex
           s-vertex
           _sw-vertex
           _nw-vertex
           n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first se-vertex) (second se-vertex)
                            (first s-vertex) (second s-vertex))
                 (.add.line scene *x *y
                            (first ne-vertex) (second ne-vertex)
                            (first se-vertex) (second se-vertex))
                 (.add.line scene +x +y
                            (first ne-vertex) (second ne-vertex)
                            (first n-vertex) (second n-vertex))]]
      (.add container (clj->js lines)))
    (let [[x y] se-neighbor
          points (.getGridPoints board x y true)
          [ne-vertex
           se-vertex
           s-vertex
           sw-vertex
           _nw-vertex
           _n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first se-vertex) (second se-vertex)
                            (first s-vertex) (second s-vertex))
                 (.add.line scene *x *y
                            (first ne-vertex) (second ne-vertex)
                            (first se-vertex) (second se-vertex))
                 (.add.line scene +x +y
                            (first sw-vertex) (second sw-vertex)
                            (first s-vertex) (second s-vertex))]]
      (.add container (clj->js lines)))
    (let [[x y] sw-neighbor
          points (.getGridPoints board x y true)
          [_ne-vertex
           se-vertex
           s-vertex
           sw-vertex
           nw-vertex
           _n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first se-vertex) (second se-vertex)
                            (first s-vertex) (second s-vertex))
                 (.add.line scene *x *y
                            (first nw-vertex) (second nw-vertex)
                            (first sw-vertex) (second sw-vertex))
                 (.add.line scene +x +y
                            (first sw-vertex) (second sw-vertex)
                            (first s-vertex) (second s-vertex))]]
      (.add container (clj->js lines)))
    (let [[x y] w-neighbor
          points (.getGridPoints board x y true)
          [_ne-vertex
           _se-vertex
           s-vertex
           sw-vertex
           nw-vertex
           n-vertex] (rex/cljs-points points)
          lines [(.add.line scene +x +y
                            (first sw-vertex) (second sw-vertex)
                            (first s-vertex) (second s-vertex))
                 (.add.line scene *x *y
                            (first nw-vertex) (second nw-vertex)
                            (first sw-vertex) (second sw-vertex))
                 (.add.line scene +x +y
                            (first nw-vertex) (second nw-vertex)
                            (first n-vertex) (second n-vertex))]]
      (.add container (clj->js lines)))
    (doseq [line (.-list container)]
      (.setStrokeStyle line width color)
      (.setLineWidth line width))
    {:container container
     :update
     (fn [[x y]]
       (.setVisible container true)
       (let [center-points (map #(vec [(.-x %) (.-y %)]) (.getGridPoints board x y true))
             [ct-x ct-y] (apply midpoint center-points)]
         (.setPosition container ct-x ct-y)))
     :reset
     (fn []
       (.setVisible container false))}))

(defn create-map-scene [scene]
  (let [{:keys [board coords game]} (.registry.get scene "game")
        center-points (map #(vec [(.-x %) (.-y %)]) (.getGridPoints board 12 12 true)) ; map center
        [ct-x ct-y] (apply midpoint center-points)
        _camera (camera/draggable-camera scene ct-x ct-y 0.5)
        gfx (init-gfx scene)
        r 64
        {highlight-space :update
         reset-highlight :reset} (draw-adjacency-highlights scene board r)]
    (let [coord->units (group-by :coord (vals (:units @game)))]
      (doall
       (for [coord coords
             :let [units (coord->units coord)
                   space (get-in @game [:spaces coord])]]
         (draw-space scene gfx board r coord space units))))
    (.setInteractive board)
    (.on board "tilemove"
         (fn [_pointer xy]
           (let [coord [(.-x xy) (.-y xy)]
                 space (get-in @game [:spaces coord])]
             (if space
               (do
                 (highlight-space coord)
                 (.events.emit scene "tilemove" space))
               (do
                 (reset-highlight)
                 (.events.emit scene "tilereset"))))))))
