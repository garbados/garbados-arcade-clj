(ns planetcall.regions-test
  (:require [planetcall.regions :as pr]
            [planetcall.geometry :as geo]
            [clojure.test :refer [is]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]))

(defspec regions-group-ok 50
  (prop/for-all
   [coords (s/gen ::geo/coords)]
   (let [regions (pr/get-regions coords)]
     (is (s/valid? ::pr/regions regions))
     (is (every? #(= % 1)
                 (map (partial pr/count-intersections regions)
                      regions))))))
