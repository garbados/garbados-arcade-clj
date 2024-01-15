(ns longtime.select-test
  (:require [longtime.select :as select]
            [clojure.test :refer [deftest]]
            [arcade.test-util :refer [spec-test-syms]]))

(deftest spec-tests
  (spec-test-syms
   [`select/passes-filter?
    `select/passes-select?
    `select/find-individuals
    `select/get-cast]))
