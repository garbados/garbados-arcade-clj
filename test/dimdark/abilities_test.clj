(ns dimdark.abilities-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.abilities :as a]))

(deftest fspec-test
  (spec-test-syms
   [`a/get-user-magnitude
    `a/get-synergy-magnitude
    `a/get-target-magnitude
    `a/needs-target?
    `a/friendly-ability-hits?
    `a/hostile-ability-hits?
    `a/resolve-effects]))

(deftest ability-details-spec-test
  (doseq [details (vals a/ability->details)]
    (testing (name (:name details))
      (is (s/valid? ::a/ability-details details)
          (s/explain-str ::a/ability-details details))))
  (doseq [[name details] a/ability->details]
    (testing (str "name matches for " name)
      (is (= name (:name details))))))
