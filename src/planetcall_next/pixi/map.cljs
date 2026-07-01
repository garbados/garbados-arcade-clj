(ns planetcall-next.pixi.map
  (:require
   ["pixi.js" :refer [Container Graphics]]
   [clojure.string :as string]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.rules.spaces :as spaces]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.utils :refer [midpoint]]))

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

(def prefix->color
  {:rotting colors/SAP-GREEN
   :volatile colors/SAFETY-YELLOW
   :shattered colors/LIGHT-RED-OCHRE
   :preserved colors/LAVENDER
   :ashen colors/DIM-GRAY})

(def suffix->color
  {:mountain colors/SILVER
   :canyon colors/DIM-GRAY
   :mesa colors/SALMON
   :steppe colors/MIKADO-YELLOW
   :marsh colors/LIME-GREEN
   :ooze colors/TEAL
   :wreckage colors/BRICK-RED
   :wastes colors/LIGHT-SALMON})

(def feature->color
  {:vents colors/DARK-ORANGE
   :ruins colors/ROMAN-SILVER
   :titan colors/ANTIQUE-RUBY
   :xenobog colors/SCREAMIN-GREEN})

(def improvement->color
  {:farm colors/LIME-GREEN
   :factory colors/BROWN
   :turbine colors/ORANGE
   :labs colors/SILVER
   :stockpile colors/ROMAN-SILVER})

(def PLANET-COLOR :cyan)
(def PLAYER-COLORS
  [:red
   :blue
   :yellow
   :green
   :purple
   :orange])

(defn draw-space [radius coord space]
  (let [-space (atom nil)
        [n-vertex
         ne-vertex
         se-vertex
         s-vertex
         sw-vertex
         nw-vertex
         :as vertices] (radius->points radius)
        center (apply midpoint vertices)
        container (new Container (clj->js {:eventMode "static"}))
        border-gfx (new Graphics)
        prefix-gfx (new Graphics)
        suffix-gfx (new Graphics)
        fungus-gfx (new Graphics)
        miasma-gfx (new Graphics)
        road-gfx (new Graphics)
        improvement-gfx (new Graphics)
        controller-gfx (new Graphics)
        feature-gfx (new Graphics)
        w (* radius sqrt3)
        h (* radius 2)
        [x y] (axial->offset coord)
        px (cond-> (* x w)
             (odd? y) (+ (/ w 2))
             :also int)
        py (* y h 0.75)
        update-space
        (fn [{:keys [prefix suffix feature miasma road fungus improvement controller] :as space}]
          (when (not= (:prefix @-space) prefix)
            (-> prefix-gfx
                (.poly (clj->js (flatten [nw-vertex n-vertex center])))
                (.fill (clj->js {:color (prefix->color prefix)}))))
          (when (not= (:suffix @-space) suffix)
            (-> suffix-gfx
                (.poly (clj->js (flatten [ne-vertex n-vertex center])))
                (.fill (clj->js {:color (suffix->color suffix)}))))
          (cond
            (and improvement (not= improvement (:improvement @-space)))
            (let [[x y] (midpoint nw-vertex n-vertex center)]
              (-> improvement-gfx
                  (.circle x y (int (/ radius 6)))
                  (.fill (clj->js {:color (improvement->color improvement)}))
                  (.stroke (clj->js {:color colors/BLACK
                                     :width 1}))))
            (and (nil? improvement) (some? (:improvement @-space)))
            (.clear improvement-gfx))
          (cond
            (and feature (not= feature (:feature @-space)))
            (let [[x y] (midpoint ne-vertex n-vertex center)]
              (-> feature-gfx
                  (.circle x y (int (/ radius 6)))
                  (.fill (clj->js {:color (feature->color feature)}))
                  (.stroke (clj->js {:color colors/BLACK
                                     :width 1}))))
            (and (nil? feature) (some? (:feature @-space)))
            (.clear feature-gfx))
          (cond
            (and controller (not= controller (:controller @-space)))
            (-> controller-gfx
                (.poly (clj->js (flatten [center ne-vertex se-vertex s-vertex])))
                (.fill (clj->js {:color (nth PLAYER-COLORS controller)
                                 :alpha 0.5})))
            (and (nil? controller) (some? (:controller @-space)))
            (.clear controller-gfx))
          (cond
            (and fungus (not (:fungus @-space)))
            (-> fungus-gfx
                (.poly (clj->js (flatten [nw-vertex
                                          center
                                          (midpoint s-vertex center)
                                          (midpoint nw-vertex sw-vertex)])))
                (.fill (clj->js {:color colors/RADICAL-RED})))
            (and (nil? fungus) (some? (:fungus @-space)))
            (.clear fungus-gfx))
          (cond
            (and road (not (:road @-space)))
            (-> road-gfx
                (.moveTo (first ne-vertex) (second ne-vertex))
                (.lineTo (first sw-vertex) (second sw-vertex))
                (.moveTo (first nw-vertex) (second nw-vertex))
                (.lineTo (first s-vertex) (second s-vertex))
                (.stroke (clj->js {:color colors/BROWN
                                   :width 3})))
            (and (nil? road) (some? (:road @-space)))
            (.clear road-gfx))
          (cond
            (and miasma (not (:miasma @-space)))
            (-> miasma-gfx
                (.poly (clj->js (flatten [s-vertex
                                          (midpoint s-vertex center)
                                          (midpoint nw-vertex sw-vertex)
                                          sw-vertex])))
                (.fill (clj->js {:color colors/TEAL
                                 :alpha 0.5})))
            (and (nil? miasma) (some? (:miasma @-space)))
            (.clear miasma-gfx))
          (reset! -space space))
        mask-gfx (-> (new Graphics)
                     (.poly (clj->js (flatten vertices)) true)
                     (.fill (clj->js {:color colors/WHITE})))]
    (-> border-gfx
        (.poly (clj->js (flatten vertices)) true)
        (.stroke (clj->js {:color colors/BLACK
                           :width 2})))
    (update-space space)
    (set! (.-mask container) mask-gfx)
    (.addChild container
               mask-gfx
               prefix-gfx
               suffix-gfx
               fungus-gfx
               improvement-gfx
               controller-gfx
               road-gfx
               miasma-gfx
               feature-gfx
               border-gfx)
    (pixi/move-to container [px py])
    {:container container
     :update update-space}))

(defn create-spaces-view [radius game]
  (let [container (new Container)
        coords (keys (:spaces game))
        space-views
        (reduce
         (fn [space-views {coord :coord :as space-view}]
           (assoc space-views coord space-view))
         {}
         (for [coord coords
               :let [space (get-in game [:spaces coord])
                     {space-container :container :as space-view}
                     (draw-space radius coord space)]]
           (do
             (.addChild container space-container)
             (assoc space-view :coord coord))))]
    {:container container
     :coord->view space-views}))

(defn create-space-tooltip [w h font-size & {:keys [top right]}]
  (let [container (new Container)
        text-style {:fontSize font-size
                    :fill colors/WHITE
                    :align "right"
                    :wordWrap true
                    :wordWrapWidth (- w (* 2 right))}
        text (pixi/->text "" text-style)]
    (.anchor.set text 1 0)
    (pixi/move-to text [(- w right) top])
    (let [bg-gfx (-> (new Graphics)
                     (.rect 0 0 (+ w (* 2 right)) (+ h (* 2 top)))
                     (.fill (clj->js {:color colors/BLACK}))
                     (.stroke (clj->js {:color colors/WHITE
                                        :width 2})))]
      (.addChild container bg-gfx text))
    (set! (.-visible container) false)
    {:container container
     :update
     (fn [game {:keys [coord prefix suffix feature improvement controller] :as space}]
       (if space
         (do
           (set! (.-visible container) true)
           (let [coord-s
                 (str "[" (string/join ", " coord) "]")
                 space-name
                 (let [base-name (->> [prefix suffix]
                                      (map name)
                                      (map string/capitalize)
                                      (string/join " "))]
                   (if feature
                     (str base-name ", " (string/capitalize (name feature)))
                     base-name))
                 bools-s
                 (->> (select-keys space [:fungus :miasma :road])
                      (filter (comp true? second))
                      (map (comp name first))
                      (map string/capitalize)
                      (string/join ", "))
                 yield-s
                 (->> (spaces/space-yield space)
                      (filter (comp pos-int? second))
                      (map #(string/join " " [(name (first %))
                                              (second %)]))
                      (map string/capitalize)
                      (string/join ", "))
                 improvement-s
                 (if (and improvement controller)
                   (let [faction-name (get-in game [:factions controller :name])]
                     (string/capitalize (string/join " " [faction-name (name improvement)])))
                   "")
                 s
                 (string/join "\n" [coord-s space-name bools-s yield-s improvement-s])]
             (set! (.-text text) s)))
         (set! (.-visible container) false)))}))

(defn create-units-tooltip [w h font-size & {:keys [top right]}]
  (let [container (new Container)
        unit1-container (new Container)
        unit2-container (new Container)
        unit3-container (new Container)
        containers [unit1-container unit2-container unit3-container]
        text-style {:fontSize font-size
                    :fill colors/WHITE
                    :align "right"
                    :wordWrap true
                    :wordWrapWidth (- w (* 2 right))}
        unit1-text (pixi/->text "" text-style)
        unit2-text (pixi/->text "" text-style)
        unit3-text (pixi/->text "" text-style)
        text-objects [unit1-text unit2-text unit3-text]]
    (doseq [i (range 3)
            :let [text (nth text-objects i)
                  text-container (nth containers i)
                  bg-gfx (-> (new Graphics)
                             (.rect 0 0 (+ w (* 2 right)) (+ h (* 2 top)))
                             (.fill (clj->js {:color colors/BLACK}))
                             (.stroke (clj->js {:color colors/WHITE
                                                :width 2})))]]
      (.anchor.set ^js/Object text 1 0)
      (pixi/move-to text [(- w right) top])
      (.addChild ^js/Object text-container bg-gfx text)
      (pixi/move-to text-container [0 (* (+ h (* 2 top)) i)])
      (.addChild ^js/Object container text-container)
      (set! (.-visible text-container) false))
    {:container container
     :update
     (fn [game units]
       (doseq [i (range 3)
               :let [container (nth containers i)
                     text (nth text-objects i)]]
         (if-let [unit (nth units i nil)]
           (let [unit-name
                 (let [color (nth PLAYER-COLORS (:faction unit))
                       faction-name (string/capitalize (name color))]
                   (str faction-name " " (:name unit)))
                 traits
                 (str "[" (string/join ", " (map name (:traits unit))) "]")
                 arms-resolve
                 (str "Arms: " (:arms unit) ", Resolve: " (:resolve unit))
                 integrity-moves
                 (str "Moves: " (:moves unit) " / " (:max-moves unit)
                      ", HP: " (:integrity unit) " / " (:max-integrity unit))
                 s
                 (string/join "\n" [unit-name traits arms-resolve integrity-moves])]
             (set! (.-text text) s)
             (set! (.-visible container) true))
           (set! (.-visible container) false))))}))

(defn create-map-view [app -game]
  (let [radius 32
        w 300 h 144
        top 4 right 4
        map-container (new Container)
        {space-tooltip-container :container
         update-space-tooltip :update} (create-space-tooltip w h 20 :top top :right right)
        {unit-tooltip-container :container
         update-units-tooltip :update} (create-units-tooltip w h 20 :top top :right right)
        spaces-view (create-spaces-view radius @-game)
        board-container (->> (:container spaces-view)
                             (pixi/scrollable-container app)
                             (pixi/zoomable-container))]
    (doseq [[coord {container :container}] (:coord->view spaces-view)]
      (.on container "pointerover"
           (fn [_]
             (let [space (get-in @-game [:spaces coord])
                   units (filter #(= (:coord %) coord) (vals (:units @-game)))]
               (update-space-tooltip @-game space)
               (update-units-tooltip @-game units)))))
      ;; (.scale.set board-container 2)
    (let [offset [(-> app .-screen .-width (- radius) (/ 2))
                  (-> app .-screen .-height (- radius) (/ 2))]]
      (pixi/move-to board-container offset))
    (let [offset [(-> app .-screen .-width) 0]]
      (pixi/anchor-container offset space-tooltip-container 1 0))
    (let [offset [(-> app .-screen .-width) (+ 2 (* 4 top) (* 2 h))]]
      (pixi/anchor-container offset unit-tooltip-container 1 -1))
    (.addChild map-container
               board-container
               space-tooltip-container
               unit-tooltip-container)
    map-container))
