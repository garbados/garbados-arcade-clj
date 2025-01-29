(ns dimdark.encounters-test
  (:require
   [arcade.test-util :refer [spec-test-syms]]
   [clojure.test :refer [deftest testing]]
   [dimdark.encounters :as e]
   [dimdark.kobolds :as k]))

(deftest fspec-test
  (spec-test-syms
   [`e/update-creature
    `e/assoc-creature
    `e/is-monster?
    `e/get-possible-targets
    ;; `e/get-usable-abilities
    ;; `e/effects+magnitude->effects
    ;; `e/clear-magnitude-effects
    ;; `e/pre-magnitude-effects
    ;; `e/merge-effect
    ;; `e/merge-effects
    ;; `e/expand-rolled-effects
    ;; `e/calc-impacts
    ;; `e/resolve-instant-effects
    ;; `e/resolve-impacts
    ;; `e/round-effects-tick
    ;; `e/env-effects-tick
    ;; `e/turn-effects-tick
    ;; `e/remove-dead-monsters
    ;; `e/victory?
    ;; `e/defeat?
    ;; `e/front-line-crumples?
    ;; `e/crumple-front-line
    ;; `e/next-round
    ;; `e/next-turn
    ]))

(deftest auto-turn-test
  (testing "Basic random strategy works"
    (let [[kobolds1 kobolds2] (partition 3 (shuffle (map k/kobold->creature (vals k/kobolds))))
          encounter (e/init-encounter kobolds1 kobolds2)
          impacts (e/auto-turn encounter)]
      (println impacts))))
