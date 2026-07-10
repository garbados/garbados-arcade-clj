(ns planetcall.core-test
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [clojure.test :refer [deftest is testing]]
            [planetcall.abilities :as pb]
            [planetcall.actions :as pa]
            [planetcall.ai.random :as rand-ai]
            [planetcall.games :as pg]
            [planetcall.gathering :refer [do-gather-phase]]
            [planetcall.geometry :refer [get-adjacent-coords]]
            [planetcall.ideotech :refer [can-research]]
            [planetcall.planet :as planet]
            [planetcall.radiation :as radiation]
            [planetcall.weather :as weather]))

(deftest game-plays-ok
  (let [game (g/generate (s/gen ::pg/game))]
    (testing "Mock game passes muster."
      (is (s/valid? ::pg/game game)))
    (testing "Beginning faction has valid research options."
      (let [researched (-> game :factions first :researched)
            options (can-research researched)
            answer #{"s2b" "e1" "i1" "s2a" "m1" "c1"}]
        (is (= answer options))))
    (testing "Researching forbids other tech."
      (let [researched (get-in game [:factions 0 :researched])
            researched* (into researched #{"s4a"})]
        (is (not (some #{"i1"} (can-research researched*))))))
    (testing "Researching forbids other tech (idiomatic)."
      (let [game* (reduce #(pg/complete-research %1 0 %2)
                          game
                          ["s2a" "s3a" "s4a"])
            researched (get-in game* [:factions 0 :researched])]
        (is (not (some #{"i1"} (can-research researched))))))
    (testing "Selecting research works."
      (let [game* (pg/select-research game 0 "e1")]
        (is (s/valid? ::pg/game game*))))
    (testing "Gathering works."
      (let [game* (do-gather-phase game 0)]
        (is (= 2 (-> game* :factions (nth 0) :stockpiles seq first second :energy)))
        (is (s/valid? ::pg/game game*))))
    (testing "Gathering works with many improvements."
      (let [claimed (-> game :factions (nth 0) :claimed first get-adjacent-coords)
            improvements [:farm :workblock :reactor :reactor :laboratory :laboratory]
            game* (->
                   (reduce
                    (fn [game [coord improvement]]
                      (-> game
                          (pg/claim-space 0 coord)
                          (assoc-in [:coord->space coord :improvement]
                                    improvement)))
                    game
                    (map vector claimed improvements))
                   (do-gather-phase 0))]
        (is (false? (get-in game* [:factions 0 :conditions :scarcity])))))
    (testing "Action listing works."
      (let [player 0
            actions (pa/available-actions game player)]
        (s/valid? ::pa/action-listing actions)))
    (let [do-one-action
          (fn [game player]
            (let [[id args] (rand-ai/choose-action game player)]
              (apply pa/do-action game player id args)))
          do-action-phase
          (fn [game player]
            (reduce
             (fn [game _]
               (if (= 0 (get-in game [:turn-info :action-points]))
                 (reduced game)
                 (do-one-action game player)))
             game
             (-> nil constantly repeatedly)))
          do-movement-phase
          (fn [game player]
            (reduce
             (fn [game _]
               (if-let [needs-orders (seq (pb/needs-orders game player))]
                 (let [unit-path (rand-nth needs-orders)
                       [ability choices] (rand-ai/choose-ability game unit-path)]
                   (apply pb/do-ability game unit-path ability choices))
                 (reduced game)))
             game
             (repeatedly (constantly nil))))
          do-one-turn
          (fn [game player]
            (-> game
                (do-gather-phase player)
                (assoc-in [:turn-info :action-points]
                          (pa/get-action-points game player))
                (rand-ai/choose-research player)
                (do-action-phase player)
                (do-movement-phase player)
                (update :turn inc)))]
      (testing "A turn works."
        (let [player 0]
          (is (s/valid? ::pg/game (do-one-turn game player)))))
      (testing "Many turns work."
        (let [game* (reduce
                     do-one-turn
                     game
                     (take 100 (cycle (-> game :factions count range))))]
          (is (s/valid? ::pg/game game*))))
      (testing "A round works"
        (let [do-faction-turns
              (fn [game]
                (reduce do-one-turn game
                        ((comp range count :factions) game)))
              do-one-round
              (fn [game]
                (-> game
                    pg/diminish-world-conditions
                    radiation/radiation-hurts
                    radiation/radiation-spreads
                    weather/miasma-moves
                    planet/spawn-phase
                    ;; TODO planet movement phase
                    do-faction-turns
                    pg/record-player-victories))
              game*
              (reduce
               (fn [game i]
                 (println (str "ROUND " i))
                 (if (:done? game)
                   (do
                     (println "GAME OVER")
                     (println "VICTORS:" (:winners game))
                     (reduced game))
                   (do-one-round game)))
               game
               (range 100))]
          (is (s/valid? ::pg/game game*)
              (s/explain-str ::pg/game game*)))))))
