(ns planetcall.planet
  (:require [clojure.set :refer [difference]]
            [clojure.spec.alpha :as s]
            [planetcall.games :as pg]
            [planetcall.units :as pu]))

;; planet only has movement phase actions
;; but has a chance to create one or more units each turn

(defn is-unclaimed [factions coord]
  {:post [#(s/valid? boolean? %)]}
  (every?
   nil?
   (for [{claimed :claimed} factions]
     (claimed coord))))

(def wildlife
  (reduce
   (fn [all name]
     (let [design {:loadout name
                   :chassis name
                   :mods #{}}]
       (assoc all name design)))
   {}
   [:loper
    :wormswarm
    :razorbeak
    :bloodgnat
    :oathwyrm
    :draconaut
    :bowerholm
    :curator
    :troll
    :hewer]))

(s/def ::wildlife (-> wildlife keys set))

(def wildlife-by-cataclysm-level
  [[:loper :wormswarm :razorbeak]
   [:bloodgnat :draconaut :curator]
   [:oathwyrm :bowerholm :troll :hewer]])

(defn spawn-phase
  "1. decide how many units to spawn (0-1 plus cataclysm, up to 1/10 of the map size)
   2. decide what kind of units to spawn, which must have valid spawn points
   3. spawn chosen units at available points"
  [{:keys [factions world coords coord->space] :as game}]
  (let [cataclysm (pg/cataclysm-level world)
        unit-floor (+ cataclysm (rand-int 2))
        unit-ceiling (int (/ (count coords) 10))
        unit-n (if (> unit-ceiling unit-floor)
                 (rand-nth (range unit-floor unit-ceiling))
                 unit-floor)
        candidates (flatten
                    (take (+ 1 cataclysm)
                          wildlife-by-cataclysm-level))
        unit-types (map
                    (fn [_] (rand-nth candidates))
                    (range unit-n))
        unclaimed (reduce difference #{} (map :claimed factions))
        [unclaimed-fungus
         unclaimed-aquatic]
        (reduce
         (fn [[unclaimed-fungus unclaimed-aquatic] coord]
           (let [vegetation (get-in coord->space [coord :vegetation])
                 elevation (get-in coord->space [coord :elevation])]
             (cond
               (-> elevation (- (pg/cataclysm-level world)) (< 0))
               [unclaimed-fungus (cons coord unclaimed-aquatic)]
               (= vegetation :fungus)
               [(cons coord unclaimed-fungus) unclaimed-aquatic]
               :else
               [unclaimed-fungus unclaimed-aquatic])))
         [[] []]
         unclaimed)]
    (reduce
     (fn [game unit-type]
       (let [design (wildlife unit-type)
             aquatic? (-> design :chassis :ability (= :embark))
             options (if aquatic? unclaimed-aquatic unclaimed-fungus)]
         (if (not-empty options)
           (let [coord (rand-nth options)
                 unit (pu/design->unit design -1)]
             (update-in game [:coord->units coord]
                        #(if %
                           (vec (cons unit %))
                           [unit])))
           game)))
     game
     unit-types)))

(s/fdef spawn-phase
  :args (s/cat :game ::pg/game)
  :ret ::pg/game)

(defn movement-phase [game] 'todo)