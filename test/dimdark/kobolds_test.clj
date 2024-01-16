(ns dimdark.kobolds-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.kobolds :as k]))

(deftest fspec-test
  (spec-test-syms
   [`k/equipment-stats
    `k/equippable?
    `k/kobold-stat
    `k/kobold->stats]))

(deftest kobolds-spec-test
  (testing "Kobolds conform to spec."
    (is (s/valid? ::k/kobolds k/kobolds)
        (s/explain-str ::k/kobolds k/kobolds))))

(deftest equippable-starting-gear-test
  (testing "Kobolds can equip their own starting gear."
    (doseq [kobold (vals k/kobolds)
            :let [equipped (vals (:equipped kobold))]]
      (doseq [equipment equipped]
        (is (k/equippable? kobold equipment))))))

(deftest class-growth-test
  (testing "Kobold growth patterns use valid number of points"
    (doseq [[klass growth] k/kobold-class-growth]
      (is (= 18 (reduce + 0 (vals growth)))
          (str klass " uses wrong number of points!")))))
