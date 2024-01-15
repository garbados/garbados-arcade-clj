(ns planetcall.games-test
  (:require [planetcall.games :as pg]
            [clojure.test :refer [is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as prop]
            [clojure.spec.alpha :as s]))

(defspec games-init-ok 20
  (prop/for-all
   [game (s/gen ::pg/game)]
   (let [new-game (pg/init-game (game :coord->space)
                                (game :coord->units)
                                (game :factions))]
     (is (s/valid? ::pg/game new-game)))))
