(ns ambition.bots-test 
  (:require
    [ambition.bots :as bots]
    [ambition.core :as core]
    [clojure.spec.alpha :as spec]
    [clojure.test :refer [deftest is]]))

(deftest play-auto-game
  (doseq [_ (range 10)
          :let [players
                (->> (concat bots/bots bots/bots)
                     (shuffle)
                     (take 4)
                     (map-indexed vector))
                world
                (reduce
                 (fn [world _]
                   (if (get-in world [:meta :done?] false)
                     (reduced world)
                     (as-> (core/begin-round world) $
                       (reduce
                        (fn [world [player player-fn]]
                          (if (core/eliminated? world player)
                            world
                            (let [[action option] (player-fn world player)
                                  {:keys [do!]} (get core/ACTION-METHODS action)]
                              (do! world option))))
                        $
                        players)
                       (core/end-round $))))
                 core/WORLD
                 (repeat nil))]]
    (is
     (spec/valid? ::core/world world)
     (spec/explain-str ::core/world world))))