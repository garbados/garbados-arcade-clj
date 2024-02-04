(ns dimdark.encounters
  (:require [arcade.utils :as u]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [dimdark.abilities :as a]
            [dimdark.core :as d]
            [dimdark.combat :as c]))

(s/def ::kobolds (s/coll-of ::d/creature :max-count 4))
(s/def ::monsters (s/coll-of ::d/creature :max-count 4))
(s/def ::kobolds-env ::d/environment)
(s/def ::monsters-env ::d/environment)
(s/def ::turn-order (s/coll-of ::d/creature :max-count 8))
(s/def ::turn ::d/creature)
(s/def ::round pos-int?)
(s/def ::encounter
  (s/keys :req-un [::kobolds
                   ::monsters
                   ::kobolds-env
                   ::monsters-env
                   ::turn-order
                   ::turn
                   ::round]))

(s/def ::impacts
  (s/map-of (s/or :env #{:kobolds-env :monsters-env}
                  :creature ::d/creature)
            ::d/effects))

(defn get-possible-targets
  [{:keys [kobolds monsters]} {:keys [row] :as creature} ability]
  (let [{:keys [traits]} (a/ability->details ability)
        monster? (u/contains-v? monsters creature)
        friendlies (if monster? monsters kobolds)
        hostiles (if monster? kobolds monsters)]
    (when-not (contains? traits :passive)
      (cond->>
       (cond
         (contains? traits :self) [creature]
         (and (contains? traits :close) (= :back row)) []
         (and (contains? traits :ranged) (= :front row)) []
         (contains? traits :environmental) []
         (contains? traits :hostile) hostiles
         (contains? traits :friendly) friendlies)
        (or (contains? traits :close)
            (contains? traits :front-row)) (filter #(= :front (:row %)))
        (contains? traits :back-row) (filter #(= :back (:row %)))))))

(defn get-usable-abilities [encounter creature]
  (for [ability (a/filter-active (:abilities creature))
        :let [targets (get-possible-targets encounter creature ability)]
        :when (seq targets)]
    ability))

(s/fdef get-usable-abilities
  :args (s/cat :encounter ::encounter
               :creature ::d/creature))

(s/fdef get-possible-targets
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :creature ::d/creature
                 :ability ::a/ability)
          #(g/fmap
            (fn [encounter]
              (let [group (rand-nth [:kobolds :monsters])
                    creature (rand-nth (get encounter group))
                    ability (rand-nth (get-usable-abilities encounter creature))]
                [encounter creature ability]))
            (s/gen ::encounter))))

(defn calc-impacts [{:keys [monsters]} creature ability target]
  (let [{:keys [traits effects self-effects env-effects]} (a/ability->details ability)
        monster? (u/contains-v? monsters creature)
        roll-magnitude
        (fn [target]
          (cond
            (contains? traits :hostile) (a/hostile-ability-hits? ability creature target)
            (contains? traits :friendly) (a/friendly-ability-hits? ability creature target)
            :else (a/self-ability-magnitude ability creature)))
        target+magnitude->effects
        (fn [target magnitude]
          (cond-> {}
            (seq effects)
            (assoc target (into {} (for [[effect coefficient] effects
                                   :let [impact (int (* coefficient magnitude))]
                                   :when (pos-int? impact)]
                                     [effect impact])))))
        add-misc-effects
        #(cond-> %
           (seq self-effects)
           (assoc creature
                  (let [self-magnitude (a/self-ability-magnitude ability creature)]
                    (into {} (for [[effect coefficient] self-effects
                                   :let [impact (int (* coefficient self-magnitude))]
                                   :when (pos-int? impact)]
                               [effect impact]))))
           (and (seq env-effects)
                (contains? traits :hostile))
           (assoc (if monster? :kobolds-env :monsters-env)
                  (let [env-magnitude (a/self-ability-magnitude ability creature)]
                    (into {} (for [[effect coefficient] env-effects
                                   :let [impact (int (* coefficient env-magnitude))]
                                   :when (pos-int? impact)]
                               [effect impact])))))]
    (if (seq? target)
      (->>
       (for [target target
             :let [magnitude (roll-magnitude target)]
             :when (pos-int? magnitude)]
         [target (target+magnitude->effects target magnitude)])
       (into {})
       add-misc-effects)
      (let [magnitude (roll-magnitude target)]
        (add-misc-effects (target+magnitude->effects target magnitude))))))

(s/fdef calc-impacts
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :creature ::d/creature
                 :ability ::a/ability
                 :target ::d/creature)
          #(g/fmap
            (fn [encounter]
              (let [group (rand-nth [:kobolds :monsters])
                    creature (rand-nth (get encounter group))
                    ability (rand-nth (get-usable-abilities encounter creature))
                    target (rand-nth (get-possible-targets encounter creature ability))]
                [encounter creature ability target]))
            (s/gen ::encounter)))
  :ret ::impacts)

(defn merge-effect [magnitude magnitude*]
  (cond
    (and (pos-int? magnitude)
         (pos-int? magnitude*)) (max magnitude magnitude*)
    (and (pos-int? magnitude)
         (neg-int? magnitude*)) (- magnitude magnitude*)
    (and (neg-int? magnitude)
         (pos-int? magnitude*)) (+ magnitude magnitude*)
    (and (neg-int? magnitude)
         (neg-int? magnitude*)) (min magnitude magnitude*)
    :else magnitude*))

(def compound-effect +)

(defn merge-effects [creature effects]
  (as-> creature $
    (reduce
     (fn [creature effect]
       (update-in creature [:effects effect] merge-effect (get effects effect)))
     $ [:mending :taunted :hidden :purged :cleansed :delayed
        :bleeding :nauseous :burning :scorched :charmed
        :sharpened :focused :reinforced :blessed :quickened :laden])
    (reduce
     (fn [creature effect]
       (update-in creature [:effects effect] compound-effect (get effects effect)))
     $ [:poisoned :chilled :frozen :marked :empowered])))

(s/fdef merge-effects
  :args (s/cat :creature ::d/creature
               :effects ::d/effects)
  :ret ::d/creature)

(defn round-effects-tick
  "Diminishes and disjoins effects that apply when a round begins."
  [encounter]
  (let [update-effects
        (fn [{:keys [effects] :as creature}]
          (cond-> creature
            (contains? effects :delayed) (update :effects disj :delayed)))]
    (-> encounter
        (update :kobolds (partial map update-effects))
        (update :monsters (partial map update-effects)))))

(s/fdef round-effects-tick
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn turn-effects-tick [{:keys [effects] :as creature}]
  (cond->
   (->> d/diminishing-effects
        (filter #(contains? effects %))
        (reduce
         (fn [creature effect]
           (let [magnitude (dec (get effects effect))]
             (if (zero? magnitude)
               (update creature :effects disj effect)
               (assoc-in creature [:effects effect] magnitude))))
         creature))
    (contains? effects :burning) (update :health - 3)
    (contains? effects :bleeding) (update :health - 3)
    (contains? effects :mending) (update :health + (:mending effects))
    (contains? effects :poisoned) (update :health - (:poisoned effects))
    (contains? effects :marked) (->
                                 (update :health - (:marked effects))
                                 (update :effects disj :marked))
    :also (update :health max 0)))

(s/fdef turn-effects-tick
  :args (s/cat :creature ::d/creature)
  :ret ::d/creature)

(defn pre-magnitude-effects [{:keys [effects] :as creature} ability]
  (let [{:keys [traits]} (a/ability->details ability)]
    [(cond-> creature
       (contains? effects :hidden) (update :effects disj :hidden)
       (and (contains? effects :empowered)
            (contains? traits :spell)) (update :effects disj :empowered)
       (and (contains? effects :extended)
            (contains? traits :spell)) (update :effects disj :extended))
     (cond-> 0
       (contains? effects :hidden) (+ (:hidden effects))
       (and (contains? effects :empowered)
            (contains? traits :spell)) (+ (:empowered effects)))]))

(s/fdef pre-magnitude-effects
  :args (s/cat :creature ::d/creature
               :ability ::a/ability)
  :ret (s/tuple ::d/creature nat-int?))

(defn expand-rolled-effects [{:keys [stats effects]}]
  (let [{:keys [armor]} (d/stats+effects->stats stats effects)]
    (cond-> effects
      (contains? effects :damage) (assoc :damage (-> effects :damage c/roll-nd6 (c/rolls+armor->damage armor)))
      (contains? effects :healing) (assoc :healing (c/roll-nd6 (:healing effects))))))

(s/fdef expand-rolled-effects
  :args (s/cat :creature ::d/creature)
  :ret ::d/effects)

(defn resolve-instant-effects [{:keys [stats effects] :as creature}]
  (cond-> creature
    (contains? effects :damage)
    (-> (update :effects disj :damage)
        (update :health - (:damage effects)))
    (contains? effects :healing)
    (-> (update :effects disj :healing)
        (update :health + (:healing effects))
        (update :health min (:health stats)))
    (contains? effects :pushed)
    (-> (update :effects disj :pushed)
        (update :row :back))
    (contains? effects :pulled)
    (-> (update :effects disj :pulled)
        (update :row :front))
    (contains? effects :cleansed)
    (-> (update :effects disj :cleansed)
        (update :effects
                (fn [effects]
                  (as-> effects $
                    (reduce disj $ d/negative-effects)
                    (reduce
                     (fn [effects effect]
                       (cond-> effects
                         (contains? effects effect)
                         (max (get effects effect 0) 0)))
                     $ d/stat-effects)))))
    (contains? effects :purged)
    (-> (update :effects disj :purged)
        (update :effects
                (fn [effects]
                  (as-> effects $
                    (reduce disj $ d/positive-effects)
                    (reduce
                     (fn [effects effect]
                       (cond-> effects
                         (contains? effects effect)
                         (min (get effects effect 0) 0)))
                     $ d/stat-effects)
                    (reduce
                     (fn [effects effect]
                       (cond-> effects
                         (zero? (get effects effect)) (disj effects effect)))
                     $ d/stat-effects)))))
    :also (update :health max 0)))

(s/fdef resolve-instant-effects
  :args (s/cat :creature ::d/creature)
  :ret ::d/creature)
