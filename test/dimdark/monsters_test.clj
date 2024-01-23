(ns dimdark.monsters-test
  (:require [arcade.test-util :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [dimdark.monsters :as m]))

(deftest fspec-test
  (spec-test-syms
   [`m/gen-monster]))
