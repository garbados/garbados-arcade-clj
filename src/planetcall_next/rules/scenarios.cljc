(ns planetcall-next.rules.scenarios 
  (:require
   [arcade.slurp :refer-macros [slurp->details]]
   [planetcall-next.rules.games :as games]
   [planetcall-next.rules.units :as units]))

(def scenario->details
  (slurp->details "resources/planetcall/scenarios/core.edn"))

(defn init-game-from-scenario
  [coords scenario]
  (let [{start-coords :coords :keys [start]} (scenario->details scenario)
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
     (range players))))
