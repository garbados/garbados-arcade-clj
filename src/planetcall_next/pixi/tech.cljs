(ns planetcall-next.pixi.tech 
  (:require
   ["pixi.js" :refer [Container Graphics]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.rules.tech :as tech]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.geometry :refer [polygon-points]]
   [planetcall.geometry :as geo]))

(defn create-ideo-circles [radius]
  (flatten
   (doall
    (for [i (range 4)
          :let [r (* (inc i) radius)
                n (+ 3 i)
                points (polygon-points [0 0] 5 r)
                point-pairs (map vector points (take 5 (drop 1 (cycle points))))]]
      (doall
       (for [j (range 5)
             :let [[[x1 y1] [x2 y2]] (nth point-pairs j)
                   color (nth colors/IDEOLOGIES j)
                   tech-circles
                   (for [k (range (dec n))
                         :let [[x y] [(geo/lerp x1 x2 (/ (inc k) n))
                                      (geo/lerp y1 y2 (/ (inc k) n))]]]
                     {:circle (-> (new Graphics)
                                  (.circle x y 9)
                                  (.fill (clj->js {:color color})))
                      :pos [x y]
                      :ideology (nth tech/ideology-names j)
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

(defn create-syng-circles [radius]
  (flatten
   (doall
    (for [i (range 3)
          :let [r (+ (* radius 0.5) (* (inc i) radius 1.075))
                n (+ 2 i)
                points (polygon-points [0 0] 5 r :rotation 120)
                point-pairs (map vector points (take 5 (drop 1 (cycle points))))]]
      (doall
       (for [j (range 5)
             :let [[[x1 y1] [x2 y2]] (nth point-pairs j)
                   color (nth colors/SYNERGIES j)
                   cramping 2
                   tech-circles
                   (for [k (range (dec n))
                         :let [[x y] [(geo/lerp x1 x2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))
                                      (geo/lerp y1 y2 (/ (+ cramping (inc k)) (+ (* 2 cramping) n)))]]]
                     {:circle (-> (new Graphics)
                                  (.circle x y 9)
                                  (.fill (clj->js {:color color})))
                      :pos [x y]
                      :ideology (nth tech/synergy-names j)
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
     :ideologies ideo-circles}))

(defn create-tech-view [app -game & {:keys [radius]
                                     :or {radius 128}}]
  (let [{tech-view-container :container} (create-tech-graph radius)
        tech-view (->> tech-view-container
                       (pixi/scrollable-container app)
                       (pixi/zoomable-container))
        offset [(-> app .-screen .-width (/ 2))
                (-> app .-screen .-height (/ 2))]]
    (pixi/move-to tech-view-container offset)
    tech-view))
