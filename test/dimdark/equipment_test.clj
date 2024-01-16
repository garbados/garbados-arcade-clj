(ns dimdark.equipment-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.core :as d]
            [dimdark.equipment :as eq]))

(deftest fspec-test
  (spec-test-syms
   [`eq/armor-level->stats
    `eq/gen-basic-equipment
    `eq/name-rare
    `eq/rand-modifier
    `eq/weapon-level->stats]))

(deftest spec-test
  (testing "modifier->details"
    (let [modifier-details-spec
          (s/map-of ::eq/modifier (s/tuple d/stats ::eq/level))]
      (is (s/valid? modifier-details-spec eq/modifier->details)
          (s/explain-str modifier-details-spec eq/modifier->details)))))