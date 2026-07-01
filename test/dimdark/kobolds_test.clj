(ns dimdark.kobolds-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.kobolds :as k]))

(deftest fspec-test
  (spec-test-syms
   [`k/equipment-stats
    `k/equippable?
    `k/kobold->creature
    `k/kobold->stats
    `k/kobold?]))

(deftest kobolds-spec-test
  (testing "Kobolds conform to spec."
    (is (s/valid? ::k/kobolds k/kobolds)
        (s/explain-str ::k/kobolds k/kobolds)))
  (testing "Kobold growth patterns use enough points."
    (doseq [[kobold-name kobold] k/kobolds
            :let [sum (reduce + 0 (vals (:growth kobold)))]]
      (is (= 20 sum)
          (str (name kobold-name) " is using " sum " points.")))))