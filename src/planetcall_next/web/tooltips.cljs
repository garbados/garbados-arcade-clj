(ns planetcall-next.web.tooltips 
  (:require
   [clojure.string :as string]
   [planetcall-next.rules.spaces :as spaces]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.config :as config]))

(set! *warn-on-infer* false)

(defn create-vert-text-objects
  [scene x n & {:keys [top right] :or {top 4 right 4}}]
  (reduce
   (fn [text-objects s]
     (let [first-object (first text-objects)
           y (if (seq text-objects)
               (+ (.-y first-object) (.-height first-object))
               top)
           text-object (.add.text scene (- x right) y s)]
       (.setOrigin text-object 1 0)
       (cons text-object text-objects)))
   []
   (repeat n "")))

(defn draw-space-tooltip [scene x y w h]
  (let [container (.add.container scene x y)
        tooltip-rect (.add.rectangle scene 0 0 w h colors/BLACK)
        [improvement-text
         bools-text
         yield-text
         space-name-text
         coord-text
         :as text-objects]
        (create-vert-text-objects scene w 5)]
    (.setOrigin tooltip-rect 0)
    (.setStrokeStyle tooltip-rect 3 colors/WHITE)
    (.add container (clj->js (cons tooltip-rect text-objects)))
    {:container container
     :rect tooltip-rect
     :coord coord-text
     :space-name space-name-text
     :bools bools-text
     :improvement improvement-text
     :yield yield-text}))

(defn draw-unit-tooltip
  [scene x y w h]
  (let [container (.add.container scene x y)
        tooltip-rect (.add.rectangle scene 0 0 w h colors/BLACK)
        [integrity-moves-text
         arms-resolve-text
         traits-text
         name-text
         :as text-objects]
        (create-vert-text-objects scene w 4)]
    (.setOrigin tooltip-rect 0)
    (.setStrokeStyle tooltip-rect 3 colors/WHITE)
    (.add container (clj->js (cons tooltip-rect text-objects)))
    {:container container
     :name name-text
     :traits traits-text
     :arms-resolve arms-resolve-text
     :integrity-moves integrity-moves-text}))

(defn make-space-tooltip
  [scene & {:keys [WIDTH HEIGHT]
            :or {WIDTH config/WIDTH HEIGHT config/HEIGHT}}]
  (let [w (/ WIDTH 3) h (/ HEIGHT 10)
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
                (map #(string/join " " [(name (first %))
                                        (second %)]))
                (string/join ", ")))}
        {container :container
         :as tooltip} (draw-space-tooltip scene x y w h)]
    {:container container
     :update
     (fn [space]
       (.setVisible container true)
       (let [{game :game} (.registry.get scene "game")]
         (doseq [[field f] transforms]
           (.setText (get tooltip field) (f game space)))))
     :reset
     (fn []
       (.setVisible container false))}))

(defn make-unit-tooltip
  [scene & {:keys [WIDTH HEIGHT]
            :or {WIDTH config/WIDTH HEIGHT config/HEIGHT}}]
  (let [w (/ WIDTH 3) h (/ HEIGHT 12)
        x (- WIDTH w)
        transforms
        {:name
         (fn [_game unit]
           (let [color (nth config/PLAYER-COLORS (:faction unit))
                 faction-name (string/capitalize (name color))]
             (str faction-name " " (:name unit))))
         :traits
         (fn [_game unit]
           (str "[" (string/join ", " (map name (:traits unit))) "]"))
         :arms-resolve
         (fn [_game unit]
           (str "Arms: " (:arms unit) ", Resolve: " (:resolve unit)))
         :integrity-moves
         (fn [_game unit]
           (str "Moves: " (:moves unit) " / " (:max-moves unit)
                ", HP: " (:integrity unit) " / " (:max-integrity unit)))}
        tooltips
        (for [i (range 3)]
          (let [y (* h i)]
            (draw-unit-tooltip scene x y w h)))]
    {:containers (map :container tooltips)
     :update
     (fn [space]
       (let [{game :game} (.registry.get scene "game")
             units (filter #(= (:coord %) (:coord space)) (vals (:units @game)))]
         (if (seq units)
           (doseq [i (range (count units))
                   :let [unit (nth units i)
                         {container :container
                          :as tooltip} (nth tooltips i)]]
             (.setVisible container true)
             (doseq [[field f] transforms]
               (.setText (get tooltip field) (f game unit))))
           (doseq [{container :container} tooltips]
             (.setVisible container false)))))
     :reset
     (fn []
       (doseq [{container :container} tooltips]
         (.setVisible container false)))}))
