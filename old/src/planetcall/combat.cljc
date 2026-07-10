(ns planetcall.combat 
  (:require [planetcall.geometry :as geo]
            [clojure.spec.alpha :as s]
            [planetcall.games :as pg]
            [planetcall.units :as pu]))

(s/def ::unit-path (s/cat :coord ::geo/coord
                          :index nat-int?))

(defn has-trait
  ([units trait]
   (some (comp trait :traits) units))
  ([units trait player]
   (has-trait
    (filter #(-> % :faction (= player)) units)
    trait)))

(defn get-nearby [game coord]
  (->> (geo/get-coords-within coord 2)
       (into #{coord})
       (map #(pg/get-units-at-coord game %))
       flatten))

(defn calc-coefficients
  ([game [coord i]]
   (let [unit (pg/get-unit-by-index game [coord i])
         nearby  (get-nearby game coord)
         has-trait (partial has-trait nearby)
         is-psychic (-> unit :traits :psychic)
         has-psi-field (has-trait nearby :psi-field)
         has-dampener (has-trait nearby :dampening)
         modifier (+ (if is-psychic 1/2 0)
                     (if has-psi-field 1/2 0)
                     (if has-dampener -1/2 0))]
     [(- 1 modifier)
      (+ 1/2 modifier)]))
  ([game [coord1 i] [coord2 j]]
   (let [[a b] (calc-coefficients game [coord1 i])]
     (if (-> game :coord->units (get coord2) (get j) :traits :psychic)
       [(- a 1/2) (+ b 1/2)]
       [a b]))))

(defn calc-advantages
  ([game [coord i]]
   {:pre [(some? (pg/get-unit-by-index game [coord i]))]}
   (let [unit (pg/get-unit-by-index game [coord i])
         nearby (get-nearby game coord)
         supported? (has-trait nearby :supporting (:faction unit))
         reinforced (get-in unit [:conditions :reinforced] 0)]
     (+ (if supported? 1 0)
        reinforced)))
  ([game [coord1 i] [coord2 j]]
   {:pre [(some? (pg/get-unit-by-index game [coord1 i]))
          (some? (pg/get-unit-by-index game [coord2 j]))]}
   (let [attacker (pg/get-unit-by-index game [coord1 i])
         defender (pg/get-unit-by-index game [coord2 j])
         trooped? (and (-> attacker :traits :trooper)
                       (-> defender :faction (= -1)))]
     (+ (if trooped? 1 0)
        (calc-advantages game [coord1 i])))))

(s/fdef calc-advantages
  :args (s/cat :game ::pg/game
               :unit ::unit-path
               :defender ::pu/unit)
  :ret ::pg/game)

(defn get-strength [unit advantages a b]
  (* (+ (-> unit :arms (* a))
        (-> unit :resolve (* b)))
     (+ 1
        (/ advantages 4))))

(s/fdef get-strength
  :args (s/cat :unit ::pu/unit
               :advantages number?
               :a number?
               :b number?)
  :ret number?)

(defn calc-strength
  ([game unit-path & [other]]
   {:pre [(some? (pg/get-unit-by-index game unit-path))]}
   (let [unit (pg/get-unit-by-index game unit-path)
         player (:faction unit)
         advantages (if other
                      (calc-advantages game unit-path other)
                      (calc-advantages game unit-path))
         [a b] (if other
                 (calc-coefficients game unit-path other)
                 (calc-coefficients game unit-path))
         strength (get-strength unit advantages a b)]
     (if (and
          (> player -1)
          (some? (pg/get-faction-condition game player :scarcity)))
       (int (/ strength 2))
       strength))))

(defn roll-dn [n] (-> n rand-int inc))

(defn roll-harm [strength]
  (+ (roll-dn strength)
     (roll-dn strength)))

(defn harm-unit [game [coord i] harm]
  (update-in game [:coord->units coord i :integrity] #(- % harm)))

(defn confront
  "Confrontation by a static strength value or between a pair of units."
  ([game strength defender]
   (->> (calc-strength game defender)
        (- strength)
        (max 1)
        roll-harm
        (harm-unit game defender)))
  ([game [attacker defender]]
   (let [strength1 (calc-strength game attacker defender)
         strength2 (calc-strength game defender attacker)
         strength (max 1 (- strength1 strength2))
         harm (roll-harm strength)]
     (harm-unit game defender harm))))

(s/fdef confront
  :args (s/cat :game ::pg/game
               :rest (s/or
                      :static
                      (s/cat :strength pos-int?
                             :defender ::unit-path)
                      :dynamic
                      (s/tuple ::unit-path ::unit-path)))
  :ret ::pg/game)

(defn find-aerial-defenders [game coord base-defenders]
  (let [controlling-players
        (->> (pg/get-units-at-coord game coord)
             (map :faction)
             set)]
    (concat
     (filter
      (fn [[coord i]]
        (let [unit (pg/get-unit-by-index game [coord i])]
          (or (-> unit :traits :aerial)
              (-> unit :traits :reach))))
      base-defenders)
     (reduce
      concat
      (for [coord2 (-> game :coord->units keys)]
        (for [i ((comp range count) (get-in game [:coord->units coord2]))
              :let [unit (get-in game [:coord->units coord2 i])
                    ok-player? (controlling-players (unit :faction))
                    intercepting? (contains? (get-in unit [:conditions :intercepting]) coord)]
              :when (and ok-player? intercepting?)]
          [coord2 i]))))))

(defn select-defender [game [coord1 i] coord]
  (let [attacker (pg/get-unit-by-index game [coord1 i])
        defenders* (map
                    #(vec [coord %])
                    (-> game :coord->units (get coord) count range))
        defenders (if (-> attacker :traits :aerial)
                    (find-aerial-defenders game coord defenders*)
                    defenders*)]
    (when (seq defenders)
      (->> defenders
           (map
            (fn [[coord2 j]]
              [coord2 j (calc-strength game [coord2 j] [coord1 i])]))
           (reduce
            (fn [[coord1 i strength1] [coord2 j strength2]]
              (if (> strength1 strength2)
                [coord1 i strength1]
                [coord2 j strength2])))
           (take 2)))))

(s/fdef select-defender
  :args (s/cat :game ::pg/game
               :attacker ::unit-path
               :coord ::geo/coord)
  :ret (s/nilable ::unit-path))

(defn attack [game attacker coord]
  (let [defender (select-defender game attacker coord)
        attack* [attacker defender]
        counter [defender attacker]]
    (reduce confront game [attack* counter attack*])))

(s/fdef attack
  :args (s/cat :game ::pg/game
               :attacker ::unit-path
               :coord ::geo/coord)
  :ret ::pg/game)

(defn bombard [game [coord1 i] coord]
  (reduce
   #(confront %1 [[coord1 i] [coord %2]])
   game
   (-> game :coord->units (get coord) count range)))

(s/fdef bombard
  :args (s/cat :game ::pg/game
               :attacker ::unit-path
               :stack ::geo/coord)
  :ret ::pg/game)

(defn strike [game attacker coord]
  (let [defender (select-defender game attacker coord)]
    (if defender
      (reduce confront game [[attacker defender]
                             [defender attacker]])
      (bombard game attacker coord))))

(s/fdef strike
  :args (s/cat :game ::pg/game
               :attacker ::unit-path
               :stack ::geo/coord)
  :ret ::pg/game)