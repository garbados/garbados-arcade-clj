(ns dimdark.abilities
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]
            [clojure.set :as set]))

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
    :self
    :other
    :front-row
    :back-row
    :fire
    :frost
    :poison
    :mental
    :spell
    :physical})
(s/def ::trait traits)
(s/def ::traits (s/coll-of ::trait :kind set?))
(s/def ::coefficient
  (s/or :int (s/int-in 1 3)
        :float (s/double-in :min 0 :max 2 :infinite? false :NaN? false)))
(s/def ::stat-expr
  (s/or :one ::d/stat
        :many (s/coll-of ::d/stat :kind set? :min-count 1)))
(s/def ::uses (s/map-of ::stat-expr ::coefficient))
(s/def ::effects (s/map-of ::d/effect ::coefficient))
(s/def ::self-effects ::effects)
(s/def ::ability-details
  (s/keys :req-un [::d/name
                   ::description
                   ::traits]
          :opt-un [::uses
                   ::effects
                   ::self-effects]))

(def ability->details
  {:attack
   {:name :attack
    :description "Strike at an enemy with your weapon!"
    :traits #{:direct :close :hostile :physical}
    :effects {:damage 1}}
   :shield-bash
   {:name :shield-bash
    :description "Slam your shield into an enemy, delaying their next turn."
    :traits #{:direct :close :hostile :physical}
    :uses {:armor 2}
    :effects {:damage 0.5 :delayed 1}}
   :firebolt
   {:name :firebolt
    :description "Conjure a ball of oily fire and lob it at a foe."
    :traits #{:direct :ranged :hostile :fire :spell}
    :effects {:damage 0.8 :burning 1}}})

(def abilities (set (keys ability->details)))
(s/def ::ability abilities)

(defn get-user-magnitude [ability creature]
  (let [{:keys [traits uses]
         :or {uses {}}} (ability ability->details)
        {:keys [attack aptitude aptitudes]}
        (d/stats+effects->stats (:stats creature) (:effects creature))]
    (cond-> (reduce
             (fn [sum [stat coefficient]]
               (+ sum
                  (* coefficient
                     (get-in creature [:stats stat] 0))))
             0
             uses)
      (contains? traits :physical) (+ attack)
      (contains? traits :spell) (+ aptitude)
      (contains? traits :fire) (+ (:fire aptitudes 0))
      (contains? traits :frost) (+ (:frost aptitudes 0))
      (contains? traits :poison) (+ (:poison aptitudes 0))
      (contains? traits :mental) (+ (:mental aptitudes 0)))))

(s/fdef get-user-magnitude
  :args (s/cat :ability ::ability
               :creature ::d/creature)
  :ret number?)

(defn get-target-magnitude [ability creature]
  (let [{:keys [traits]} (ability ability->details)
        {:keys [vulns defense resistance resistances]}
        (d/stats+effects->stats (:stats creature) (:effects creature))]
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
  :args (s/cat :ability ::ability
               :creature ::d/creature)
  :ret number?)

(defn needs-target? [{:keys [traits]}]
  (cond
    (contains? traits :self) false
    (contains? traits :area) false
    :else                    true))

(s/fdef needs-target?
  :args (s/cat :ability ::ability-details)
  :ret boolean?)

(defn friendly-ability-hits? [ability user]
  (let [magnitude (get-user-magnitude ability user)]
    (if (zero? magnitude)
      0
      (int (math-log2 magnitude)))))

(s/fdef friendly-ability-hits?
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
  :args (s/cat :ability ::ability
               :user ::d/creature
               :target ::d/creature)
  :ret int?)

(defn resolve-effects [effects margin]
  (into {} (for [[effect coefficient] effects
                 :let [value (int (* margin coefficient))]
                 :when (pos-int? value)]
             [effect value])))

(s/fdef resolve-effects
  :args (s/cat :effects ::effects
               :margin number?)
  :ret (s/map-of ::d/effect nat-int?))
