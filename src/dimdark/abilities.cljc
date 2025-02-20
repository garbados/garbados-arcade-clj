(ns dimdark.abilities
  (:require
   #?(:clj [arcade.slurp :refer [inline-slurp]]
      :cljs [arcade.slurp :refer-macros [inline-slurp]])
   [clojure.edn :as edn]
   [clojure.set :as set]
   [clojure.spec.alpha :as s]
   [dimdark.core :as d]
   [dimdark.effects :as effects]))

(def clj-log2 #?(:clj (Math/log 2) :cljs nil))
(defn math-log2 [x]
  #?(:clj (/ (Math/log x) clj-log2)
     :cljs (js/Math.log2 x)))

(s/def ::description string?)
(def traits
  #{:close
    :ranged
    :piercing
    :hostile
    :friendly
    :direct
    :area
    :environmental
    :self
    :other
    :front-row
    :back-row
    :fire
    :frost
    :poison
    :mental
    :spell
    :physical
    :passive})
(s/def ::trait traits)
(s/def ::traits (s/coll-of ::trait :kind set?))
(def coefficient-max 3)
(s/def ::coefficient
  (s/or :int (s/int-in (- coefficient-max) (inc coefficient-max))
        :float (s/double-in :min (- coefficient-max)
                            :max coefficient-max
                            :infinite? false
                            :NaN? false)))
(s/def ::stat-expr
  (s/or :one ::d/stat-or-merit
        :many (s/coll-of ::d/stat-or-merit :kind set? :min-count 1)))
(s/def ::uses (s/map-of ::stat-expr ::coefficient))
(s/def ::affects (s/map-of ::d/stat-or-merit ::coefficient))
(s/def ::party-affects ::affects)
(s/def ::effects (s/map-of ::d/effect ::coefficient))
(s/def ::self-effects ::effects)
(s/def ::env-effects (s/map-of ::d/env-effect ::coefficient))
(s/def ::requires (s/coll-of ::d/effect :kind set?))
(s/def ::self-requires (s/coll-of ::d/effect :kind set?))
(s/def ::ability-details
  (s/keys :req-un [::d/name
                   ::description
                   ::traits]
          :opt-un [::uses
                   ::effects
                   ::self-effects
                   ::env-effects
                   ::affects
                   ::party-affects
                   ::requires
                   ::self-requires]))

(def universal-abilities
  {:attack
   {:name :attack
    :description "Strike at an enemy with your weapon!"
    :traits #{:direct :close :hostile :physical}
    :effects {:damage 1}}})

(def kobold-abilities
  (->> [(inline-slurp "resources/dimdark/abilities/druid.edn")
        (inline-slurp "resources/dimdark/abilities/guardian.edn")
        (inline-slurp "resources/dimdark/abilities/mage.edn")
        (inline-slurp "resources/dimdark/abilities/ranger.edn")
        (inline-slurp "resources/dimdark/abilities/sneak.edn")
        (inline-slurp "resources/dimdark/abilities/warrior.edn")]
       (map edn/read-string)
       (reduce merge {})))

(def monster-abilities
  (->> [#_(inline-slurp "resources/dimdark/abilities/goblin.edn")
        #_(inline-slurp "resources/dimdark/abilities/orc.edn")
        #_(inline-slurp "resources/dimdark/abilities/undead.edn")
        #_(inline-slurp "resources/dimdark/abilities/demon.edn")
        #_(inline-slurp "resources/dimdark/abilities/hooman.edn")
        #_(inline-slurp "resources/dimdark/abilities/spider.edn")
        #_(inline-slurp "resources/dimdark/abilities/mechini.edn")
        #_(inline-slurp "resources/dimdark/abilities/slime.edn")
        #_(inline-slurp "resources/dimdark/abilities/troll.edn")]
       (map edn/read-string)
       (reduce merge {})))

(def ability->details
  (merge universal-abilities kobold-abilities monster-abilities))

(def abilities (set (keys ability->details)))
(s/def ::ability abilities)

(defn get-user-magnitude
  [{:keys [traits uses] :or {uses {}}} {:keys [stats effects]}]
  (let [{:keys [attack aptitude aptitudes]} (effects/stats+effects->stats stats effects)]
    (cond-> (reduce
             (fn [sum [stat coefficient]]
               (+ sum (* coefficient (get stats stat 0))))
             0
             uses)
      (contains? traits :physical) (+ attack)
      (contains? traits :spell) (+ aptitude)
      (contains? traits :fire) (+ (:fire aptitudes 0))
      (contains? traits :frost) (+ (:frost aptitudes 0))
      (contains? traits :poison) (+ (:poison aptitudes 0))
      (contains? traits :mental) (+ (:mental aptitudes 0)))))

(s/fdef get-user-magnitude
  :args (s/cat :ability ::ability-details
               :creature ::d/creature)
  :ret number?)

(defn get-synergy-magnitude
  [{:keys [traits]} {:keys [stats effects]}]
  (let [{:keys [aptitudes]} (effects/stats+effects->stats stats effects)]
    (cond-> 0
      (contains? traits :fire) (+ (:fire aptitudes 0))
      (contains? traits :frost) (+ (:frost aptitudes 0))
      (contains? traits :poison) (+ (:poison aptitudes 0))
      (contains? traits :mental) (+ (:mental aptitudes 0)))))

(s/fdef get-synergy-magnitude
  :args (s/cat :ability ::ability-details
               :creature ::d/creature)
  :ret number?)

(defn get-target-magnitude
  [{:keys [traits]} {:keys [stats effects]}]
  (let [{:keys [vulns defense resistance resistances]} (effects/stats+effects->stats stats effects)]
    (if (seq (set/intersection traits (or vulns #{})))
      0
      (cond-> 0
        (contains? traits :physical) (+ defense)
        (contains? traits :spell) (+ resistance)
        (contains? traits :fire) (+ (:fire resistances 0))
        (contains? traits :frost) (+ (:frost resistances 0))
        (contains? traits :poison) (+ (:poison resistances 0))
        (contains? traits :mental) (+ (:mental resistances 0))))))

(s/fdef get-target-magnitude
  :args (s/cat :ability ::ability-details
               :creature ::d/creature)
  :ret number?)

(defn needs-target? [{:keys [traits]}]
  (or
   (contains? traits :direct)
   (not
    (or (contains? traits :self)
        (contains? traits :environmental)
        (contains? traits :area)
        (contains? traits :front-row)
        (contains? traits :back-row)))))

(s/fdef needs-target?
  :args (s/cat :ability ::ability-details)
  :ret boolean?)

(defn magnitude-bonus
  "Calculates a magnitude bonus, such as a spell benefiting from empowerment."
  [{:keys [traits]} {:keys [effects]} {target-effects :effects}]
  (cond-> 0
    (contains? effects :hidden)
    (+ (:hidden effects))
    (and (contains? effects :empowered)
         (contains? traits :spell))
    (+ (:empowered effects))
    (contains? target-effects :marked)
    (+ (:marked target-effects))))

(s/fdef magnitude-bonus
  :args (s/cat :ability ::ability-details
               :creature ::d/creature
               :target ::d/creature)
  :ret nat-int?)

(defn friendly-ability-hits? [ability user target]
  (let [magnitude (+ (get-user-magnitude ability user)
                     (get-synergy-magnitude ability target))]
    (if (zero? magnitude)
      0
      (int (math-log2 magnitude)))))

(s/fdef friendly-ability-hits?
  :args (s/cat :ability ::ability
               :user ::d/creature
               :target ::d/creature)
  :ret nat-int?)

(defn self-ability-magnitude [ability-details user]
  (let [magnitude (get-user-magnitude ability-details user)]
    (if (zero? magnitude)
      0
      (int (math-log2 magnitude)))))

(s/fdef self-ability-magnitude
  :args (s/cat :ability ::ability
               :user ::d/creature)
  :ret nat-int?)

(defn hostile-ability-hits? [ability user target]
  (let [user-magnitude (get-user-magnitude ability user)
        target-magnitude (get-target-magnitude ability target)
        magnitude (- user-magnitude target-magnitude)
        logarized (cond
                    (pos? magnitude)  (math-log2 magnitude)
                    (zero? magnitude) 0
                    :else             (- (math-log2 (- magnitude))))]
    (- (+ 11 (int logarized))
       (reduce + 0 (repeatedly 3 #(inc (rand-int 5)))))))

(s/fdef hostile-ability-hits?
  :args (s/cat :ability ::ability-details
               :user ::d/creature
               :target ::d/creature)
  :ret int?)

(defn resolve-effects [effects margin]
  (into
   {}
   (for [[effect coefficient] effects
         :let [value (int (* margin coefficient))]
         :when (pos-int? value)]
     [effect value])))

(s/fdef resolve-effects
  :args (s/cat :effects ::effects
               :margin int?)
  :ret ::effects/effects)

(defn filter-active [abilities]
  (filter #(-> % ability->details :traits (contains? :passive) not) abilities))

(s/fdef filter-active
  :args (s/cat :abilities (s/coll-of ::ability))
  :ret (s/coll-of ::ability))
