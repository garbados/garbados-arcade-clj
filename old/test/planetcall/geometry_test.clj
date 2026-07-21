(ns planetcall.geometry-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [is deftest testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [planetcall.geometry :as geo]))

(defspec adjacency-works 50
  (prop/for-all
   [coord (s/gen ::geo/coord)]
   (let [adjacent (geo/get-adjacent-coords coord)]
     (is (= 6 (count adjacent)))
     (map
      #(is (= 1 (geo/get-coords-distance coord %1)))
      adjacent))))

(defspec intersection-works 50
  (prop/for-all
   [coord (s/gen ::geo/coord)]
   (let [adjacent-coord [(first coord) (dec (second coord))]
         adjacent1 (geo/get-adjacent-coords coord)
         adjacent2 (geo/get-adjacent-coords adjacent-coord)]
     (is (geo/intersects? adjacent1 adjacent2)))))

(defspec distance-works 50
  (prop/for-all
   [coord1 (s/gen ::geo/coord)
    coord2 (s/gen ::geo/coord)]
    (let [distance (geo/get-coords-distance coord1 coord2)]
      (is (nat-int? distance)))))

(defspec coords-within-works 50
  (prop/for-all
   [coord (s/gen ::geo/coord)
    n (s/gen (s/int-in 1 100))]
    (let [within (geo/get-coords-within coord n)]
      (is (geo/intersects? #{coord} within)))))

(defspec coords-between-works 20
  (prop/for-all
   [coord1 (s/gen ::geo/coord)
    coord2 (s/gen ::geo/coord)]
    (let [between (geo/get-coords-between coord1 coord2)
          distance (geo/get-coords-distance coord1 coord2)]
      (= (count between) (max 0 (- distance 1))))))