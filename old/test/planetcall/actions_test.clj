(ns planetcall.actions-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec.alpha :as s]
            [planetcall.actions :as pa]
            [planetcall.games :as pg]
            [planetcall.ai.random :as rand-ai]))

(deftest action-details-conform
  (testing "Action details conform to spec."
    (doseq [[name action] pa/action->details]
      (is (s/assert ::pa/actions name))
      (is (s/assert ::pa/action-details action)))))

(deftest can-use-actions
  (testing "Action options work with mock game"
    (let [expected #{:construct-reactor
                     :construct-stockpile
                     :construct-farm
                     :construct-workblock
                     :forget-design
                     :construct-road
                     :disband-unit
                     :register-design
                     :pass
                     :construct-laboratory
                     :diminish-treaty
                     :expand-treaty}
          game (-> (pg/init-mock-game)
                   (assoc-in [:turn-info :action-points] 4))
          player 0
          options (pa/available-actions game player)
          option-keys (-> options keys set)]
      (and (is (= (count option-keys)
                  (count expected))
               (reduce disj expected option-keys))
           (is (= option-keys
                  expected)
               [option-keys expected])))))

(defspec random-action-ok 20
  (prop/for-all [game (s/gen ::pg/game)]
    (let [result (reduce
                  (fn [game* player]
                    (let [game** (assoc-in game* [:turn-info :action-points] 4)
                          [action args] (rand-ai/choose-action game** player)
                          effect (partial pa/do-action game** player action)]
                      (apply effect args)))
                  game
                  (take 20 (-> game :factions count range cycle)))]
      (is (s/valid? ::pg/game result)))))