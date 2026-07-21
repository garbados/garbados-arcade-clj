(ns planetcall.radiation 
  (:require [planetcall.geometry :as geo]))

;; radiation-3 spreads to every valid adjacent space

(defn radiation-spreads [{:keys [coords coord->space] :as game}]
  (reduce
   (fn [game coord]
     (update-in game [:coord->space coord :radiation]
                #(if (> 3 %) (inc %) %)))
   game
   (for [coord coords
         adj-coord (geo/get-adjacent-coords coord)
         :let [radiation (get-in coord->space [coord :radiation])
               radiation2 (get-in coord->space [adj-coord :radiation])]
         :when (and (= radiation 3)
                    (some? radiation2)
                    (not= radiation2 3))]
     adj-coord)))

(defn hurt-unit [radiation unit]
  (update unit :integrity - radiation))

(defn radiation-hurts [{:keys [coords coord->space coord->units] :as game}]
  (reduce
   (fn [game [coord radiation units]]
     (assoc-in game [:coord->units coord]
               (vec (map (partial hurt-unit radiation) units))))
   game
   (for [coord coords
         :let [radiation (get-in coord->space [coord :radiation])
               units (get coord->units coord [])]
         :when (and (> radiation 0)
                    (> (count units) 0))]
     [coord radiation units])))
