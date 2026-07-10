(ns longtime.scene-test
  (:require [clojure.test :refer [deftest]]
            [longtime.scene :as scene]
            [arcade.test-util :refer [spec-test-syms]]))

(deftest spec-tests
  (spec-test-syms
   [`scene/marshal-scene
    `scene/scene-may-occur?]))
