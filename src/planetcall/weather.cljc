(ns planetcall.weather 
  (:require [planetcall.geometry :as geo]))

(defn miasma-moves [{:keys [coord->space] :as game}]
  (->> (for [[coord1 space1] (seq coord->space)
             [coord2] (geo/get-adjacent-coords coord1)
             :let [space2 (get coord->space coord2)]
             :when (and (some? space2)
                        (= true (:miasma space1))
                        (= false (:miasma space2)))]
         [coord1 coord2])
       (reduce
        (fn [all [coord1 coord2]]
          (update all coord1
                  (fn [coll]
                    (if coll
                      #{coord2}
                      (conj coll coord2)))))
        {})
       seq
       (map
        (fn [[coord1 coords]]
          [coord1 (rand-nth coords)]))
       (reduce
        (fn [game [coord1 coord2]]
          (-> game
              (assoc-in [:coord->space coord1 :miasma] false)
              (assoc-in [:coord->space coord2 :miasma] true)))
        game)))
