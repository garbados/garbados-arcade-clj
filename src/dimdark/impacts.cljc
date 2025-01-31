(ns dimdark.impacts 
  (:require
   [clojure.spec.alpha :as s]
   [dimdark.abilities :as a]
   [dimdark.core :as d]
   [dimdark.effects :as effects]))

(s/def ::team #{:kobolds :monsters})
(s/def ::entity ::d/creature)
(s/def ::entity-map (s/keys :req-un [::team ::entity]))

(s/def ::impacts
  (s/map-of (s/or :env ::team
                  :id ::d/id)
            ::d/effects))

(defn calc-target-impact
  "Calculates the effective magnitude of an ability's effects on a target,
   returning it as an impact map."
  [{:keys [id]} {:keys [effects]} magnitude]
  (when (seq effects)
    {id (effects/calc-effects-outcomes effects magnitude)}))

(s/fdef calc-target-impact
  :args (s/cat :creature ::d/creature
               :ability ::a/ability-details
               :magnitude pos-int?)
  :ret (s/nilable ::impacts))

(defn calc-env-impact
  [{ability :name :keys [traits env-effects] :or {env-effects {}}}
   {team :team creature :entity}]
  (when (and (seq env-effects)
             (contains? traits :hostile))
    {team (effects/calc-effects-outcomes env-effects (a/self-ability-magnitude ability creature))}))

(s/fdef calc-env-impact
  :args (s/cat :ability ::ability-details
               :entity ::entity-map)
  :ret (s/nilable ::impacts))

(defn calc-self-impact
  "Calculates the effective magnitude of an ability's effect on oneself,
   returning it as an impact map."
  [{:keys [self-effects] :or {self-effects {}} :as ability}
   {:keys [id] :as creature}]
  (when (seq self-effects)
    {id (->> (a/self-ability-magnitude ability creature)
             (effects/calc-effects-outcomes self-effects))}))

(s/fdef calc-self-impact
  :args (s/cat :ability ::ability-details
               :creature ::d/creature)
  :ret (s/nilable ::impacts))

(defn roll-margin
  "Rolls for the margin of success of an ability used by a creature on another."
  [{:keys [traits] :as ability} creature target]
  (cond
    (contains? traits :hostile)
    (a/hostile-ability-hits? ability creature target)
    (contains? traits :friendly)
    (a/friendly-ability-hits? ability creature target)
    :else
    (a/self-ability-magnitude ability creature)))

(s/fdef roll-margin
  :args (s/cat :ability ::a/ability-details
               :creature ::d/creature
               :target ::d/creature)
  :ret int?)

(defn impact-target [ability-details creature target]
  (let [magnitude (roll-margin ability-details creature target)]
    (when (pos? magnitude)
      (calc-target-impact target ability-details magnitude))))

(defn calc-impacts [{creature :entity :as entity-map} ability-details targets]
  (let [impacts (merge (calc-env-impact ability-details entity-map)
                       (calc-self-impact ability-details creature))]
    (if (seq? targets)
      (reduce merge impacts
              (for [target targets]
                (impact-target ability-details creature target)))
      impacts)))

(s/fdef calc-impacts
  :args (s/cat :creature ::entity-map
               :ability ::a/ability-details
               :targets (s/coll-of ::d/creature))
  :ret ::impacts)
