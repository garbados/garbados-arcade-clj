(ns longtime.text-test 
  (:require [clojure.test :refer [deftest]]
            [arcade.test-util :refer [spec-test-syms]]
            [longtime.text :as text]))

(deftest spec-tests
  (spec-test-syms
   [`text/join-text
    `text/collect-text
    `text/quote-text
    `text/wrap-text
    `text/wrap-quote-text
    `text/wrap-options]))
