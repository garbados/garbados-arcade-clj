(ns planetcall.spaces-test
  (:require [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [planetcall.improvements :as pi]
            [planetcall.spaces :as ps]))

(defspec space-has-yield-and-upkeep 50
  (prop/for-all
   [space (s/gen ::ps/space)]
   (is (s/valid? ::ps/yield (ps/get-space-yield space)))
   (is (s/valid? ::ps/upkeep (ps/get-space-upkeep space)))))

(defspec space-has-resource-modifier 50
  (prop/for-all
   [[space resource]
    (gen/tuple (s/gen ::ps/space) (s/gen pi/primary-resources))]
   (is (int? (ps/resource->modifier space resource)))))
