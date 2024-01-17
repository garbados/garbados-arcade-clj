(ns dimdark.skills
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]
            [dimdark.kobolds :as k]))

(def clj-log2 #?(:clj (Math/log 2) :cljs nil))
(defn math-log2 [x]
  #?(:clj (/ (Math/log x) clj-log2)
     :cljs (js/Math.log2 x)))

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
  (s/or :int (s/int-in 0 3)
        :float (s/double-in :min 0 :max 2 :infinite? false :NaN? false)))
(s/def ::stat-expr
  (s/or :one ::d/stat
        :many (s/coll-of ::d/stat :kind set? :min-count 1)))
(s/def ::uses (s/map-of ::stat-expr ::coefficient))
(s/def ::effects (s/map-of ::d/effect ::coefficient))
(s/def ::self-effects ::effects)
(s/def ::skill
  (s/keys :req-un [::d/name
                   ::traits
                   ::uses
                   ::effects
                   ::self-effects]))

(defn get-stat [stat creature]
  (if (k/kobold? creature)
    (k/kobold-stat stat creature)
    (d/creature-stat stat creature)))

(defn get-user-magnitude [{:keys [uses traits]} creature]
  (cond-> (reduce
           (fn [sum [stat-expr coefficient]]
             (+ sum
                (* coefficient
                   (if (seq? stat-expr)
                     (/ (reduce
                         (fn [sum stat]
                           (+ sum (get-stat stat creature)))
                         0
                         stat-expr)
                        (count stat-expr))
                     (get-stat stat-expr creature)))))
           0
           uses)
    (contains? traits :fire) (+ (get-stat :fire-aptitude creature)
                                (get-in creature [:attributes :scales]))
    (contains? traits :frost) (+ (get-stat :frost-aptitude creature)
                                 (get-in creature [:attributes :squish]))
    (contains? traits :poison) (+ (get-stat :poison-aptitude creature)
                                  (get-in creature [:attributes :stink]))
    (contains? traits :mental) (+ (get-stat :mental-aptitude creature)
                                  (get-in creature [:attributes :brat]))))

(defn get-target-magnitude [{:keys [traits]} creature]
  (cond-> 0
    (contains? traits :spell) (+ (get-stat :resistance creature))
    (contains? traits :physical) (+ (get-stat :defense creature))
    (contains? traits :fire) (+ (get-stat :fire-resistance creature)
                                (get-in creature [:attributes :scales]))
    (contains? traits :frost) (+ (get-stat :frost-resistance creature)
                                 (get-in creature [:attributes :squish]))
    (contains? traits :poison) (+ (get-stat :poison-resistance creature)
                                  (get-in creature [:attributes :stink]))
    (contains? traits :mental) (+ (get-stat :mental-resistance creature)
                                  (get-in creature [:attributes :brat]))))

(defn use-skill-on-friendly-target [skill user]
  (let [magnitude (int (get-user-magnitude skill user))
        logarized (cond
                    (pos? magnitude) (math-log2 magnitude)
                    (zero? magnitude) 0
                    :else (- (math-log2 (- magnitude))))
        impact (int logarized)]
    impact))

(s/fdef use-skill-on-friendly-target
  :args (s/cat :skill ::skill
               :user ::d/creature)
  :ret (s/tuple boolean? int?))

(defn use-skill-on-hostile-target [skill user target]
  (let [user-magnitude (get-user-magnitude skill user)
        target-magnitude (get-target-magnitude skill target)
        magnitude (int (- user-magnitude target-magnitude))
        logarized (cond
                    (pos? magnitude) (math-log2 magnitude)
                    (zero? magnitude) 0
                    :else (- (math-log2 (- magnitude))))
        threshold (+ 10 (int logarized))
        roll (reduce + 0 (repeatedly 3 #(inc (rand-int 5))))
        diff (- threshold roll)
        success? (pos-int? diff)]
    (println logarized threshold roll)
    [success? diff]))

(s/fdef use-skill-on-hostile-target
  :args (s/cat :skill ::skill
               :user ::d/creature
               :target (s/? ::d/creature))
  :ret (s/tuple boolean? int?))

(defn use-skill-on-hostile-targets [skill user targets]
  (for [target targets]
    (use-skill-on-hostile-target skill user target)))

(s/fdef use-skill-on-hostile-targets
  :args (s/cat :skill ::skill
               :user ::d/creature
               :targets (s/coll-of ::d/creature))
  :ret (s/coll-of (s/tuple boolean? int?)))

;; TODO
(def skill->details
  {:attack
   {:name :attack
    :traits #{:direct :close :hostile :physical}
    :uses {:attack 1}
    :effects {:damage 1}
    :self-effects {}}
   :shield-bash
   {:name :shield-bash
    :traits #{:direct :close :hostile :physical}
    :uses {:attack 1}
    :effects {:damage 0.5 :delayed 0.5}
    :self-effects {}}})
