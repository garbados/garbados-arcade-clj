(ns longtime.event-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [longtime.event :as event]
            [arcade.test-util :refer [spec-test-syms]]))

(deftest spec-tests
  (spec-test-syms
   [`event/pick-event]))

(deftest test-valid-events
  (testing "All events conform to spec."
    (doseq [event (concat event/critical-events
                          event/general-events)]
      (is (s/valid? ::event/event event)
          (s/explain-str ::event/event event)))))

