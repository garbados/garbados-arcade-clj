(ns planetcall.units-test
  (:require [clojure.test :refer [is deftest testing]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [planetcall.units :as pu]))

(deftest details-conform
  (testing "Traits conform to spec."
    (doseq [[_ trait] (seq pu/trait->details)]
      (is (s/valid? ::pu/trait-detail trait))))
  (testing "Abilities conform to spec."
    (doseq [[_ ability] (seq pu/ability->details)]
      (is (s/valid? ::pu/ability-detail ability))))
  (testing "Loadouts conform to spec."
    (doseq [[_ loadout] (seq pu/loadout->details)]
      (is (s/valid? ::pu/loadout-detail loadout))))
  (testing "Chassis conform to spec."
    (doseq [[_ chassis] (seq pu/chassis->details)]
      (is (s/valid? ::pu/chassis-detail chassis))))
  (testing "Mods conform to spec."
    (doseq [[_ mod] (seq pu/mod->details)]
      (is (s/valid? ::pu/mod-detail mod)))))

(defspec designs-become-units 50
  (prop/for-all
   [design (s/gen ::pu/ok-design)
    player (s/gen nat-int?)]
   (let [unit (pu/design->unit design player)]
     (is (s/valid? ::pu/unit unit)))))

(defspec unit-gens-ok 50
  (prop/for-all
   [unit (s/gen ::pu/unit)]
   (is (s/valid? ::pu/unit unit))))
