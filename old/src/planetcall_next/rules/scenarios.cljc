(ns planetcall-next.rules.scenarios 
  (:require
   [arcade.slurp :refer-macros [slurp->details]]
   [planetcall-next.rules.games :as games]
   [planetcall-next.rules.units :as units]
   [planetcall.geometry :as geo]
   [clojure.set :as set]))

(def scenario->details
  (slurp->details "resources/planetcall/scenarios/core.edn"))

(def coord-gens
  {:hexagon (partial geo/get-coords-within [0 0])
   :triangle
   (fn [n]
    ;;  TODO
     [[0 0]
      [0 n]
      [n 0]])
   :parallelogram
   (fn [n]
    ;;  TODO
     [[0 0]
      [n n]])})

(defn init-game-from-scenario
  [scenario]
  (if-let [scenario-details (scenario->details scenario)]
    (let [{start-coords :coords :keys [shape start size]} scenario-details 
          gen-coords (get coord-gens shape)
          coords (gen-coords size)
          players (count start-coords)
          game (games/init-game coords players)]
      (reduce
       (fn [game i]
         (let [coord (nth start-coords i)
               {units :units improvement :improvement} start]
           (reduce
            (fn [game design]
              (let [unit (units/create-unit i coord design)]
                (games/realize-unit game unit)))
            (-> game
                (games/claim-space i coord)
                (games/place-improvement coord improvement))
            units)))
       game
       (range players)))
    (println (str "Unrecognized scenario: " scenario "\nKnown scenarios: " (map name (keys scenario->details))))))
