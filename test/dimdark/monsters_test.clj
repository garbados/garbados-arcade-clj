(ns dimdark.monsters-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.core :as d]
            [dimdark.monsters :as m]))

(deftest fspec-test
  (spec-test-syms
   [`m/gen-monster]))

(deftest monster-growth-spec-test
  (testing "Monster growth conforms to spec."
    (doseq [[culture growth] m/monster-growth]
      (doseq [[attr-or-merit _] growth]
        (is (s/valid? ::d/attr-or-merit attr-or-merit)
            (s/explain-str ::d/attr-or-merit attr-or-merit)))
      (let [total (reduce + 0 (vals growth))]
        (is (= 11 total)
            (str (name culture) " is using " total " points!"))))))
