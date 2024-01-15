(ns planetcall.ideotech-test
  (:require [clojure.test :refer [is deftest testing]]
            [clojure.spec.alpha :as s]
            [planetcall.ideotech :as pi]))

(deftest ideotech-details-conform
  (testing "Ideotech details conform to spec."
    (is (s/valid? ::pi/ideotech-lookup pi/ideotech->details))))
