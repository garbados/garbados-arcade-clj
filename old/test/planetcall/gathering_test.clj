(ns planetcall.gathering-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.test :refer [is]]
            [planetcall.games :as pg]
            [planetcall.gathering :refer [do-gather-phase]]))

(defspec gathering-works 20
  (prop/for-all [game (s/gen ::pg/game)]
    (let [game* (do-gather-phase game 0)]
      (and
       (is (= 2 (-> game* :factions (nth 0) :stockpiles seq first second :energy)))
       (is (s/valid? ::pg/game game*))))))