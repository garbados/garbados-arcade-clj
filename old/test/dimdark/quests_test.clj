(ns dimdark.quests-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.quests :as q]))

(deftest fspec-test
  (spec-test-syms
   [`q/quest?]))

(deftest quest-details-spec-test
  (testing "Quest details conform to spec"
    (let [spec (s/map-of ::q/quest ::q/quest-details)]
      (is (s/valid? spec q/quest->details)
          (s/explain-str spec q/quest->details)))))
