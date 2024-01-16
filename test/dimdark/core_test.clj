(ns dimdark.core-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.test :refer [deftest]]
            [dimdark.core :as d]))

(deftest fspec-test
  (spec-test-syms
   [`d/creature-stat
    `d/creature->stats]))
