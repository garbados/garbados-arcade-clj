(ns planetcall.combat-test
  (:require [clojure.test :refer [is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.spec.alpha :as s]
            [planetcall.actions :as pa]
            [planetcall.games :as pg]
            [planetcall.units :as pu]
            [planetcall.combat :as pc]
            [planetcall.ai.random :as rand-ai]))

(defspec combat-works 20
  (prop/for-all [game (s/gen ::pg/game)
                 designs (s/gen (s/coll-of ::pu/ok-design :min-count 2 :max-count 4))]
    (let [attacker-coord [0 0]
          defender-coord [0 1]
          attacker (pu/design->unit (first designs) 0)
          defenders (vec (map #(pu/design->unit % 1) (rest designs)))
          game* (-> game
                    (assoc-in [:coord->units attacker-coord] [attacker])
                    (assoc-in [:coord->units defender-coord] defenders)
                    (update-in [:treaties #{0 1}] dec))
          f (->> [[:attack pc/attack]
                  [:bombard pc/bombard]
                  [:strike pc/strike]]
                 (filter #(contains? (:abilities attacker) (first %)))
                 first
                 second)]
      (if f
        (is (s/valid? ::pg/game
                      (f game* [attacker-coord 0] defender-coord)))
        true))))