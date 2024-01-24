(ns dimdark.combat-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.test :refer [deftest]]
            [dimdark.combat :as c]))

(deftest fspec-test
  (spec-test-syms
   [`c/get-turn-order
    `c/roll-damage
    `c/rolls+armor=>damage]))
