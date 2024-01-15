(ns planetcall.improvements-test
  (:require [clojure.test :refer [deftest testing is]]
            [clojure.spec.alpha :as s]
            [planetcall.improvements :as pi]))

(deftest improvements-conform
  (testing "All improvements conform to spec."
    (doseq [[_ improvement] (seq pi/improvement->details)]
      (is (s/valid? ::pi/improvement improvement)))))
