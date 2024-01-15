(ns longtime.dream-test 
  (:require [clojure.test :refer [deftest]]
            [arcade.test-util :refer [spec-test-syms]]
            [longtime.dream :as dream]))

(deftest spec-tests
  (spec-test-syms
   [`dream/marshal-dream
    `dream/pick-dream]))
