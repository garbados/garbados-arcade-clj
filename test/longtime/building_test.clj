(ns longtime.building-test
  (:require [longtime.building :as building]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [longtime.core :as core]))

(deftest validate-buildings
  (testing "Buildings conform to spec."
    (doseq [building core/buildings
            :let [info (building/building->info building)]]
      (is (s/valid? ::building/building-info info))))
  (testing "All buildings are covered."
    (is (nil?
         (seq
          (reduce
           disj
           core/buildings
           (keys building/building->info)))))
    (is (nil?
         (seq
          (reduce
           disj
           (set (keys building/building->info))
           core/buildings))))))
