(ns planetcall-next.rules.units 
  (:require
   [clojure.set :refer [union]]
   [arcade.slurp :refer-macros [slurp->details]]))

(def chassis->details
  (slurp->details "resources/planetcall/units/chassis.edn"))

(def loadout->details
  (slurp->details "resources/planetcall/units/loadouts.edn"))

(def mod->details
  (slurp->details "resources/planetcall/units/mods.edn"))

(def trait->details
  (slurp->details "resources/planetcall/units/traits.edn"))

(defn expand-design [{:keys [loadout chassis mods]}]
  [(loadout->details loadout)
   (chassis->details chassis)
   (map mod->details mods)])

(defn get-design-traits
  ([design]
   (apply get-design-traits (expand-design design)))
  ([loadout-details chassis-details mods-details]
   (union
    (reduce union (map #(:traits % #{}) mods-details))
    (:traits loadout-details #{})
    (:traits chassis-details #{}))))

(defn integrity-from-traits [traits-details]
  (reduce
   (fn [integrity {n :integrity :or {n 0}}]
     (+ integrity n))
   10
   traits-details))

(defn arms-from-loadout+traits [{arms :arms} traits-details]
  (reduce
   (fn [arms {n :arms :or {n 0}}]
     (+ arms n))
   arms
   traits-details))

(defn moves-from-chassis+traits [{moves :moves} traits-details]
  (reduce
   (fn [moves {n :moves :or {n 0}}]
     (+ moves n))
   moves
   traits-details))

(defn resolve-from-traits [traits]
  (reduce + 1 (map (comp :resolve trait->details) traits)))

(defn get-design-cost
  ([design]
   (apply get-design-cost (expand-design design)))
  ([{arms :arms} {moves :moves} mods-details]
   (+ (* 2 arms)
      (* 2 moves)
      (->> (map :cost mods-details)
           (reduce + (count mods-details))))))

(defn get-design-upkeep
  ([design]
   (apply get-design-upkeep (expand-design design)))
  ([loadout-details chassis-details mods-details]
   (let [cost (get-design-cost loadout-details chassis-details mods-details)
         traits (get-design-traits loadout-details chassis-details mods-details)
         coef (->> (map (comp :upkeep trait->details) traits)
                   (filter some?)
                   (reduce * (/ 1 6)))]
     (max 1 (int (* cost coef))))))

(defn create-unit
  [faction coord design]
  (let [details (expand-design design)
        traits (apply get-design-traits details)
        traits-details (map trait->details traits)
        max-integrity (integrity-from-traits traits-details)
        max-moves (moves-from-chassis+traits (second details) traits-details)
        base-resolve (resolve-from-traits traits)]
    {:id (random-uuid)
     :coord coord
     :faction faction
     :design design
     :traits traits
     :integrity max-integrity
     :max-integrity max-integrity
     :moves 0 ; summoning sickness
     :max-moves max-moves
     :resolve base-resolve
     :base-resolve base-resolve
     :arms (arms-from-loadout+traits (first details) traits-details)
     :cooldowns {}
     :upkeep (get-design-upkeep design :traits traits)}))

#_(s/fdef design->unit
  :args (s/cat :design :unit/design)
  :ret :unit/unit)
