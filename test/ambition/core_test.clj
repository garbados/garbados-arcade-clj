(ns ambition.core-test
  (:require
   [ambition.core :as core]
   [arcade.test-utils :refer [test-game!]]
   [clojure.test :refer [deftest is]]))

(deftest test-game
  (test-game! "ambition"))

(deftest game-can-end
  (is
   true
   (:done?
    (core/end-round
     (reduce
      (fn [world coord]
        (assoc-in world [:spaces coord :claimant] false))
      core/WORLD
      core/COORDS)))))
