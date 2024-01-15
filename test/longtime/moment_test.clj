(ns longtime.moment-test 
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [longtime.moment :as moment]
            [longtime.scene :as scene]
            [arcade.test-util :refer [spec-test-syms]]))

(deftest test-valid-moments
  (testing "Moments conform to scene spec."
    (doseq [scene moment/moment-scenes]
      (is (s/valid? ::scene/scene scene)
          (s/explain-str ::scene/scene scene)))))

(deftest spec-tests
  (spec-test-syms
   [`moment/gen-moments]))
