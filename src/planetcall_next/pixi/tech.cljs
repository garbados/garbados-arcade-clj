(ns planetcall-next.pixi.tech 
  (:require
   ["pixi.js" :refer [Container Graphics]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.rules.tech :as tech]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.geometry :refer [polygon-points]]
   [planetcall.geometry :as geo]))

(defn create-circles
  [tiers base-techs radius-fn colors names 
   & {:keys [rotation cramping]
      :or {rotation 0 cramping 0}}]
  (flatten
   (doall
    (for [i (range tiers)
          :let [r (radius-fn i)
                n (+ base-techs i)
                points (polygon-points [0 0] 5 r :rotation rotation)
                point-pairs (map vector points (take 5 (drop 1 (cycle points))))]]
      (doall
       (for [j (range 5)
             :let [[[x1 y1] [x2 y2]] (nth point-pairs j)
                   color (nth colors j)
                   tech-circles
                   (for [k (range n)
                         :let [[x y]
                               [(geo/lerp x1 x2 (/ (+ cramping (inc k))
                                                   (+ (* 2 cramping)
                                                      (inc n))))
                                (geo/lerp y1 y2 (/ (+ cramping (inc k))
                                                   (+ (* 2 cramping)
                                                      (inc n))))]]]
                     {:circle (-> (new Graphics)
                                  (.circle x y 9)
                                  (.fill (clj->js {:color color})))
                      :pos [x y]
                      :ideology (nth names j)
                      :level (inc i)
                      :n k})
                   [[x1 y1] [x2 y2]] (map :pos ((juxt first last) tech-circles))
                   tech-line (-> (new Graphics)
                                 (.moveTo x1 y1)
                                 (.lineTo x2 y2)
                                 (.stroke (clj->js {:color colors/WHITE
                                                    :width 3})))]]
         {:circles tech-circles
          :line tech-line}))))))

(defn create-ideo-circles [radius]
  (create-circles 4
                  2
                  #(* (inc %) radius)
                  colors/IDEOLOGIES
                  tech/ideology-names))

(defn create-syng-circles [radius]
  (create-circles 3
                  1
                  #(+ (* radius 0.5) (* (inc %) radius 1.075))
                  colors/SYNERGIES
                  tech/synergy-names
                  :rotation 120
                  :cramping 2))

(defn create-tech-graph [radius]
  (let [container (new Container)
        ideo-circles (create-ideo-circles radius)
        syng-circles (create-syng-circles radius)]
    (doseq [{circles :circles line :line} ideo-circles]
      (.addChild container line)
      (doseq [{circle :circle} circles]
        (.addChild container circle)))
    (doseq [{circles :circles line :line} syng-circles]
      (.addChild container line)
      (doseq [{circle :circle} circles]
        (.addChild container circle)))
    {:container container
     :ideologies (concat ideo-circles syng-circles)}))

(defn create-tech-tooltip
  [w & {:keys [top right]}]
  (let [tech-title (pixi/->text "" {:fill colors/WHITE
                                    :fontWeight "bold"
                                    :fontSize 25})
        effect-text (pixi/->text "" {:fill colors/WHITE
                                     :fontSize 22
                                     :wordWrap true
                                     :wordWrapWidth (- w (* 2 right))})
        effect-rect (new Graphics)
        flavor-text (pixi/->text "" {:fill colors/WHITE
                                     :fontStyle "italic"
                                     :fontSize 20
                                     :align "right"
                                     :wordWrap true
                                     :wordWrapWidth (- w (* 2 right))})
        flavor-rect (new Graphics)
        container (new Container)]
    (.addChild container effect-rect tech-title effect-text flavor-rect flavor-text)
    (.anchor.set tech-title 0 1)
    (.anchor.set effect-text 0 1)
    (.anchor.set flavor-text 1)
    (pixi/move-to effect-text [right (- top)])
    (pixi/move-to flavor-text [(- right) (- top)])
    {:container container
     :update
     (fn [[x y] details]
       (set! (.-visible container) true)
       (pixi/move-to container [x y])
       (set! (.-text tech-title) (tech/tech-name details))
       (set! (.-text effect-text) (tech/explain-tech details))
       (set! (.-text flavor-text) (:flavor details ""))
       (let [w (+ (.-width flavor-text) (* 2 right))
             h (+ (.-height flavor-text) (* 2 top))]
         (-> flavor-rect
             (.clear)
             (.rect 0 0 w h)
             (.fill (clj->js {:color colors/BLACK}))
             (.stroke (clj->js {:color colors/WHITE
                                :width 2}))
             (pixi/move-to [(- w) (- h)])))
       (let [w (+ (* 2 right) (max (.-width tech-title) (.-width effect-text)))
             h (+ (.-height tech-title) (.-height effect-text) (* 2 top))]
         (-> effect-rect
             (.clear)
             (.rect 0 0 w h)
             (.fill (clj->js {:color colors/BLACK}))
             (.stroke (clj->js {:color colors/WHITE
                                :width 2})))
         (pixi/move-to tech-title [right (- 0 (.-height effect-text) top)])
         (pixi/move-to effect-rect [0 (- h)])))
     :reset
     (fn []
       (set! (.-visible container) false))}))

(defn create-tech-view [app -game & {:keys [radius]
                                     :or {radius 128}}]
  (let [{tech-view-container :container
         tech-circles :ideologies} (create-tech-graph radius)
        tech-view (->> tech-view-container
                       (pixi/scrollable-container app)
                       (pixi/zoomable-container))
        {tooltip-container :container
         update-tooltip :update
         reset-tooltip :reset} (create-tech-tooltip 500 :top 5 :right 5)
        offset [(-> app .-screen .-width (/ 2))
                (-> app .-screen .-height (/ 2))]
        container (new Container)]
    (pixi/move-to container offset)
    (doseq [{circles :circles} tech-circles]
      (doseq [{circle :circle
               ideology :ideology
               level :level
               n :n
               pos :pos} circles
              :let [details (get-in tech/ideograph [ideology level n])]]
        (set! (.-eventMode circle) "static")
        (.on circle "pointerover" (partial update-tooltip pos details) )
        (.on circle "pointerout" reset-tooltip)))
    (.addChild container tech-view tooltip-container)
    container))
