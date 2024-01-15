(ns planetcall.abilities
  (:require [clojure.spec.alpha :as s]
            [planetcall.actions :as pa]
            [planetcall.combat :as pc]
            [planetcall.games :as pg]
            [planetcall.geometry :as geo]
            [planetcall.units :as pu]))

(s/def ::game-unit-args
  (s/cat :game ::pg/game
         :unit-path ::pc/unit-path
         :rest (s/* any?)))

(s/def ::game-unit-fn
  (s/fspec
   :args ::game-unit-args
   :ret ::pg/game))

(s/def ::options ::game-unit-fn)
(s/def ::enabled?
  (s/fspec :args ::game-unit-args
           :ret boolean?))
(s/def ::prompt
  (s/keys :req-un [::pa/validator
                   ::options
                   ::pa/description]))
(s/def ::prompts
  (s/coll-of ::prompt))
(s/def ::cost (s/fspec :args ::game-unit-args
                       :ret nat-int?))
(s/def ::effect ::game-unit-fn)
(s/def ::ability-detail
  (s/keys :req-un [::effect
                   ::enabled?
                   ::cost
                   ::prompts]
          :opt-un [::pu/description
                   ::pu/cooldown]))

(defn aquatic? [{:keys [world coord->space]} coord]
  (-> coord
      coord->space
      :elevation
      (- (pg/cataclysm-level world))
      (< 0)))

(def above-water? (complement aquatic?))

(defn move-unit [game [coord1 i] coord2]
  (let [unit (pg/get-unit-by-index game [coord1 i])]
    (-> game
        (update-in [:coord->units coord1]
                   (fn [units]
                     (vec (concat (subvec units 0 i)
                                  (subvec units (inc i))))))
        (update-in [:coord->units coord2] conj unit))))

(defn unit-movement-cost [game unit-index coord]
  (let [unit (pg/get-unit-by-index game unit-index)]
    (pg/get-space-movement-cost game coord unit)))

(defn unit-movement-total [game unit-index & _]
  (:movement (pg/get-unit-by-index game unit-index)))

(def travel
  {:prompts
   [{:description "Travel where?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{player :faction} (pg/get-unit-by-index game [coord i])]
         (filter
          (fn [coord2]
            (and (above-water? game coord2)
                 (pg/is-friendly-coord? game player coord2)))
          (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost unit-movement-cost
   :effect move-unit})

(def embark
  {:prompts
   [{:description "Travel where?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{player :faction} (pg/get-unit-by-index game [coord i])]
         (filter
          (fn [coord2]
            (and (aquatic? game coord2)
                 (pg/is-friendly-coord? game player coord2)))
          (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost unit-movement-cost
   :effect move-unit})

(def hover
  {:prompts
   [{:description "Travel where?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{player :faction} (pg/get-unit-by-index game [coord i])]
         (filter (partial pg/is-friendly-coord? game player)
                 (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost (constantly 1)
   :effect move-unit})

(def intercept
  {:prompts []
   :enabled? (constantly true)
   :cost
   (fn [game unit-index]
     (:speed (pg/get-unit-by-index game unit-index)))
   :effect
   (fn [game [coord i]]
     (let [speed (:speed (pg/get-unit-by-index game [coord i]))
           intercepting (set (pg/get-real-coords game (geo/get-coords-within coord speed)))]
       (assoc-in game [:coord->units coord i :conditions :intercepting] intercepting)))})

(def attack
  {:prompts
   [{:description "Attack who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction} (pg/get-unit-by-index game [coord i])]
         (filter
          (partial pg/is-enemy-coord? game faction)
          (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost unit-movement-total
   :effect pc/attack})

(def bombard
  {:prompts
   [{:description "Bombard who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction} (pg/get-unit-by-index game [coord i])
             area-of-effect (geo/get-coords-within coord 2)]
         (filter
          (partial pg/is-enemy-coord? game faction)
          (pg/get-real-coords game area-of-effect))))}]
   :enabled? (constantly true)
   :cost unit-movement-total
   :effect pc/bombard})

(def strike
  {:prompts
   [{:description "Strike who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction
              speed   :speed} (pg/get-unit-by-index game [coord i])
             area-of-effect (geo/get-coords-within coord speed)]
         (filter
          (partial pg/is-enemy-coord? game faction)
          (pg/get-real-coords game area-of-effect))))}]
   :enabled? (constantly true)
   :cost unit-movement-total
   :effect pc/strike})

(def disable
  {:prompts
   [{:description "Disable who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction} (pg/get-unit-by-index game [coord i])]
         (filter
          (partial pg/is-enemy-coord? game faction)
          (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost (constantly 2)
   :effect
   (fn [game _unit-index coord]
     (let [inc* #(if % (inc %) 1)
           stun #(update-in % [:conditions :stunned] inc*)
           update* #(vec (map stun %))]
       (update-in game [:coord->units coord] update*)))})

(def subvert
  {:prompts
   [{:description "Subvert who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction} (pg/get-unit-by-index game [coord i])]
         (filter
          (partial pg/is-enemy-coord? game faction)
          (pg/get-real-adjacent game coord))))}]
   :enabled? (constantly true)
   :cost (constantly 2)
   :effect
   (fn [game unit-index coord]
     (let [controller (:faction (pg/get-unit-by-index game unit-index))
           update-units
           (fn [units]
             (vec (map (if (= 1 (count units))
                         #(assoc-in % [:conditions :subverted] controller)
                         (partial (pc/confront game 5)))
                       units)))]
       (update-in game [:coord->units coord] update-units)))})

(def psychout
  {:prompts
   [{:description "Psychout who?"
     :validator ::geo/coord
     :options
     (fn [game [coord _i]]
       (let [area-of-effect (geo/get-coords-within coord 2)
             has-units?
             (fn [coord]
               (let [units (pg/get-units-at-coord game coord)]
                 (> (count units) 0)))]
         (filter
          has-units?
          (pg/get-real-coords game area-of-effect))))}]
   :enabled? (constantly true)
   :cost (constantly 2)
   :effect
   (fn [game [coord1 i] coord2]
     (let [unit (pg/get-unit-by-index game [coord1 i])
           units (pg/get-units-at-coord game coord2)
           is-friendly? (partial pg/is-friendly? game (:faction unit))
           units* (vec
                   (for [unit2 units
                         :let [op (if (is-friendly? (:faction unit2)) + -)]]
                     (update unit2 :resolve op 2)))]
       (assoc-in game [:coord->units coord2] units*)))})

(defn remove-at [coll i]
  (concat
   (subvec coll 0 i)
   (subvec coll (inc i))))

(def settle
  {:prompts []
   :enabled?
   (fn [game [coord i]]
     (let [{player :faction} (pg/get-unit-by-index game [coord i])
           claimed? (contains? (get-in game [:factions player :claimed]) coord)]
       (not claimed?)))
   :cost unit-movement-total
   :effect
   (fn [game [coord i]]
     (let [{player :faction} (pg/get-unit-by-index game [coord i])
           construct-stockpile (get-in pa/action->details [:construct-stockpile :effect])]
       (-> game
           (update-in [:coord->units coord] remove-at i)
           (pg/claim-space player coord)
           (construct-stockpile player coord))))})

(def strategize
  {:prompts
   [{:description "Reinforce who?"
     :validator ::geo/coord
     :options
     (fn [game [coord i]]
       (let [{faction :faction} (pg/get-unit-by-index game [coord i])
             area-of-effect (geo/get-coords-within coord 2)]
         (for [coord (pg/get-real-coords game area-of-effect)
               :let [units (pg/get-units-at-coord game coord)]
               :when (and (> (count units) 0)
                          (pg/is-friendly-coord? game faction coord))]
           coord)))}]
   :enabled? (constantly true)
   :cost (constantly 2)
   :effect
   (fn [game [coord1 i] coord2]
     (let [unit (pg/get-unit-by-index game [coord1 i])
           units (pg/get-units-at-coord game coord2)
           is-friendly? (partial pg/is-friendly? game (:faction unit))
           units* (vec
                   (for [unit2 units
                         :let [reinforced (get-in unit2 [:conditions :reinforced] 0)
                               friendly? (is-friendly? unit2)]]
                     (if friendly?
                       (assoc-in unit2 [:conditions :reinforced] (+ reinforced 1))
                       unit2)))]
       (assoc-in game [:coord->units coord2] units*)))})

(def wait
  {:description "Spend all remaining movement points to do nothing."
   :prompts []
   :enabled? (constantly true)
   :cost unit-movement-total
   :effect (fn [game & _] game)})

(def ability->details
  (merge-with merge
   {:travel travel
    :embark embark
    :hover hover
    :intercept intercept
    :attack attack
    :bombard bombard
    :strike strike
    :disable disable
    :subvert subvert
    :psychout psychout
    :settle settle
    :strategize strategize
    :wait wait}
   (select-keys pu/ability->details [:description :cooldown])))

(s/valid? (s/map-of ::pu/abilities ::ability-detail) ability->details)

(defn apply-cost [game [coord i] cost]
  (let [unit (pg/get-unit-by-index game [coord i])
        min-zero (partial max 0)
        minus-cost #(- % cost)
        unit* (update unit :movement (comp min-zero minus-cost))]
    (assoc-in game [:coord->units coord i] unit*)))

(defn do-ability [game [coord i] ability & args]
  (let [cost (apply (-> ability ability->details :cost) game [coord i] args)
        effect #(apply (-> ability ability->details :effect) % [coord i] args)
        cooldown (get-in ability->details [ability :cooldown] 0)]
    (-> game
        (apply-cost [coord i] cost)
        (assoc-in [:coord->units coord i :cooldowns ability] cooldown)
        effect)))

(defn can-use [game [coord i]]
  (let [{movement  :movement
         abilities :abilities} (pg/get-unit-by-index game [coord i])]
    (->> (cons :wait abilities)
         (filter #((-> % ability->details :enabled?) game [coord i]))
         (filter
          (fn [ability]
            (let [cost-fn (-> ability ability->details :cost)
                  options-fn (-> ability ability->details :prompts first :options)]
              (when options-fn
                (let [options (options-fn game [coord i])
                      cost (reduce min (inc movement) (map cost-fn options))]
                  (and (< 0 (count options))
                       (<= cost movement))))))))))

(defn needs-orders [{:keys [coord->units]} player]
  (reduce
   concat
   (for [[coord units] coord->units
         :let [unit-paths
               (for [i (-> units count range)
                     unit (get units i)
                     :when (and (pg/is-controlled-unit? player unit)
                                (< 0 (:movement unit)))]
                 [coord i])]
         :when (< 0 (count unit-paths))]
     unit-paths)))

(s/fdef needs-orders
  :args (s/cat :game ::pg/game
               :player ::pg/player)
  :ret (s/coll-of ::pc/unit-path))