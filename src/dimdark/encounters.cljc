(ns dimdark.encounters
  (:require #?(:clj [arcade.text :refer [inline-slurp]]
               :cljs [arcade.text :refer-macros [inline-slurp]])
            [arcade.utils :as u]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [dimdark.abilities :as a]
            [dimdark.combat :as c]
            [dimdark.core :as d]))

(def env-effect->effects
  (edn/read-string
   (inline-slurp
    "resources/dimdark/env-effects.edn")))

(s/def ::kobolds (s/coll-of ::d/creature :max-count 4 :min-count 3))
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

(defn update-creature [encounter creature f]
  (let [{:keys [kobolds monsters turn-order turn]} encounter
        monster? (u/contains-v? monsters creature)
        i (if monster?
            (u/indexOf monsters creature)
            (u/indexOf kobolds creature))
        j (u/indexOf turn-order creature)
        creature* (f creature)]
    (cond-> encounter
      monster? (assoc-in [:monsters i] creature*)
      (not monster?) (assoc-in [:kobolds i] creature*)
      (= turn creature) (assoc :turn creature*)
      (nat-int? j) (assoc-in [:turn-order j] creature*))))

(s/def ::update-creature-fn
  (s/fspec :args (s/cat :creature ::d/creature)
           :ret ::d/creature))

(s/fdef update-creature
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :creature ::d/creature
                 :f ::update-creature-fn)
          #(g/fmap
            (fn [[encounter f]]
              (let [creature (rand-nth (:kobolds encounter))]
                [encounter creature f]))
            (s/gen (s/tuple ::encounter ::update-creature-fn))))
  :ret ::encounter)

(defn assoc-creature [encounter creature creature*]
  (update-creature encounter creature (constantly creature*)))

(s/fdef assoc-creature
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :creature ::d/creature
                 :creature* ::d/creature)
          #(g/fmap
            (fn [[encounter creature*]]
              (let [creature (rand-nth (:kobolds encounter))]
                [encounter creature creature*]))
            (s/gen (s/tuple ::encounter ::d/creature))))
  :ret ::encounter)

(defn is-monster? [encounter creature]
  (u/contains-v? (:monsters encounter) creature))

(s/fdef is-monster?
  :args (s/cat :encounter ::encounter
               :creature ::d/creature)
  :ret boolean?)

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

(defn effects+magnitude->effects [effects magnitude]
  (into {} (for [[effect coefficient] effects
                 :let [impact (int (* coefficient magnitude))]
                 :when (pos-int? impact)]
             [effect impact])))

(defn clear-magnitude-effects [{:keys [effects] :as creature} ability]
  (let [{:keys [traits]} (a/ability->details ability)]
    (cond-> creature
      (contains? effects :hidden) (update :effects dissoc :hidden)
      (and (contains? effects :empowered)
           (contains? traits :spell)) (update :effects dissoc :empowered)
      (and (contains? effects :extended)
           (contains? traits :spell)) (update :effects dissoc :extended))))

(s/fdef clear-magnitude-effects
  :args (s/cat :creature ::d/creature
               :ability ::a/ability)
  :ret ::d/creature)

(defn pre-magnitude-effects [{:keys [effects]} ability target]
  (let [target-effects (:effects target)
        {:keys [traits]} (a/ability->details ability)]
    [(cond-> target
       (contains? target-effects :marked) (update :effects dissoc :marked))
     (cond-> 0
       (contains? effects :hidden)
       (+ (:hidden effects))
       (and (contains? effects :empowered)
            (contains? traits :spell))
       (+ (:empowered effects))
       (contains? target-effects :marked)
       (+ (:marked target-effects)))]))

(s/fdef pre-magnitude-effects
  :args (s/cat :creature ::d/creature
               :ability ::a/ability)
  :ret (s/tuple ::d/creature nat-int?))

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

(s/fdef merge-effect
  :args (s/cat :magnitude int?
               :magnitude* int?)
  :ret boolean?)

(def compound-effect +)

(defn merge-effects [creature effects]
  (as-> creature $
    (reduce
     (fn [creature effect]
       (update-in creature [:effects effect] merge-effect (get effects effect)))
     $ [:mending :hidden :purged :cleansed :delayed
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

(defn expand-rolled-effects [stats effects]
  (let [{:keys [armor]} (d/stats+effects->stats stats effects)]
    (cond-> effects
      (contains? effects :damage) (assoc :damage (-> effects :damage (c/roll 6) (c/rolls+armor->damage armor)))
      (contains? effects :healing) (assoc :healing (reduce + 0 (-> effects :healing (c/roll 4)))))))

(s/fdef expand-rolled-effects
  :args (s/cat :stats ::d/stats
               :effects ::d/effects)
  :ret ::d/effects)

(s/def ::impacts
  (s/map-of (s/or :env #{:kobolds-env :monsters-env}
                  :self #{:self-effects}
                  :creature ::d/creature)
            ::d/effects))

(defn calc-impacts [encounter creature ability target]
  (let [{:keys [traits effects self-effects env-effects]} (a/ability->details ability)
        monster? (is-monster? encounter creature)
        roll-magnitude
        (fn [target magnitude]
          (cond-> magnitude
            (contains? traits :hostile) (+ (a/hostile-ability-hits? ability creature target))
            (contains? traits :friendly) (+ (a/friendly-ability-hits? ability creature target))
            :else (+ (a/self-ability-magnitude ability creature))))
        target+magnitude->target-impact
        (fn [{:keys [stats] :as target} magnitude]
          (cond-> {}
            (seq effects)
            (assoc target (into {} (expand-rolled-effects stats (effects+magnitude->effects effects magnitude))))))
        add-misc-effects
        #(cond-> %
           (seq self-effects)
           (assoc :self-effects
                  (->> (a/self-ability-magnitude ability creature)
                       (effects+magnitude->effects self-effects)
                       (expand-rolled-effects (:stats creature))
                       (into {})))
           (and (seq env-effects)
                (contains? traits :hostile))
           (assoc (if monster? :kobolds-env :monsters-env)
                  (->> (a/self-ability-magnitude ability creature)
                       (effects+magnitude->effects env-effects)
                       (into {}))))]
    (if (seq? target)
      (let [[encounter* target-impacts]
            (reduce
             (fn [[encounter target-impacts] target]
               (let [[target* magnitude] (pre-magnitude-effects creature ability target)
                     magnitude* (roll-magnitude target* magnitude)]
                 (if (pos-int? magnitude*)
                   [(assoc-creature encounter target target*)
                    (merge target-impacts (target+magnitude->target-impact target* magnitude*))]
                   [encounter target-impacts])))
             [encounter {}]
             target)
            creature* (clear-magnitude-effects creature ability)]
        [creature*
         (assoc-creature encounter* creature creature*)
         (add-misc-effects target-impacts)])
      (if (contains? traits :self)
        [creature encounter (add-misc-effects {})]
        (let [[target* magnitude] (pre-magnitude-effects creature ability target)
              magnitude* (roll-magnitude target* magnitude)
              creature* (clear-magnitude-effects creature ability)]
          [creature*
           (-> encounter
               (assoc-creature creature creature*)
               (assoc-creature target target*))
           (if (pos-int? magnitude*)
             (add-misc-effects (target+magnitude->target-impact target* magnitude*))
             {})])))))

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
  :ret (s/tuple ::d/creature ::encounter ::impacts))

(defn resolve-instant-effects [{:keys [stats effects] :as creature}]
  (cond-> creature
    (contains? effects :damage)
    (-> (update :effects dissoc :damage)
        (update :health - (:damage effects)))
    (contains? effects :healing)
    (-> (update :effects dissoc :healing)
        (update :health + (:healing effects))
        (update :health min (:health stats)))
    (contains? effects :pushed)
    (-> (update :effects dissoc :pushed)
        (update :row :back))
    (contains? effects :pulled)
    (-> (update :effects dissoc :pulled)
        (update :row :front))
    (contains? effects :cleansed)
    (-> (update :effects dissoc :cleansed)
        (update :effects
                (fn [effects]
                  (let [x (:cleansed effects)]
                    (as-> effects $
                      (reduce dissoc $ d/negative-effects)
                      (reduce
                       (fn [effects effect]
                         (if (contains? effects effect)
                           (let [y (get effects effect 0)
                                 z (min (+ y x) 0)]
                             (cond
                               (pos-int? y) effects
                               (neg-int? y)
                               (if (zero? z)
                                 (dissoc effects effect)
                                 (assoc effects effect z))))
                           effects))
                       $ d/stat-effects))))))
    (contains? effects :purged)
    (-> (update :effects dissoc :purged)
        (update :effects
                (fn [effects]
                  (let [x (:purged effects)]
                    (as-> effects $
                      (reduce dissoc $ d/positive-effects)
                      (reduce
                       (fn [effects effect]
                         (if (contains? effects effect)
                           (let [y (get effects effect 0)
                                 z (max (- y x) 0)]
                             (cond
                               (pos-int? y) effects
                               (neg-int? y)
                               (if (zero? z)
                                 (dissoc effects effect)
                                 (assoc effects effect z))))
                           effects))
                       $ d/stat-effects))))))
    :also (update :health max 0)))

(s/fdef resolve-instant-effects
  :args (s/cat :creature ::d/creature)
  :ret ::d/creature)

(defn resolve-impacts [encounter creature {:keys [self-effects] :as impacts}]
  (let [creature*
        (if self-effects
          (resolve-instant-effects (merge-effects creature self-effects))
          creature)
        creatures*
        (->> (keys impacts)
             (filter (fn [creature] (not (keyword? creature))))
             (map #(vec [% (resolve-instant-effects (merge-effects % (get impacts %)))])))
        encounter*
        (reduce
         (fn [encounter [creature creature*]]
           (assoc-creature encounter creature creature*))
         (if self-effects
           (assoc-creature encounter creature creature*)
           encounter)
         creatures*)]
    [creature*
     creatures*
     encounter*])
  )

(s/fdef resolve-impacts
  :args (s/cat :encounter ::encounter
               :creature ::d/creature
               :impacts ::impacts)
  :ret ::encounter)

(defn round-effects-tick
  "Diminishes and dissociates effects that apply when a round begins, after turn order is decided."
  [encounter]
  (let [update-effects
        (fn [{:keys [effects] :as creature}]
          (cond-> creature
            (contains? effects :delayed) (update :effects dissoc :delayed)))]
    (-> encounter
        (update :kobolds (comp vec (partial map update-effects)))
        (update :monsters (comp vec (partial map update-effects))))))

(s/fdef round-effects-tick
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn env-effects-tick [encounter creature]
  (let [env-key (if (is-monster? encounter creature)
                  :monsters-env
                  :kobolds-env)
        env-effects (get encounter env-key {})
        env-effects*
        (cond-> env-effects
          (contains? env-effects :jawtrapped)
          (dissoc :jawtrapped)
          (contains? env-effects :mawtrapped)
          (dissoc :mawtrapped))
        creature*
        (reduce
         (fn [creature [env-effect magnitude]]
           (let [effects (env-effect->effects env-effect)]
             (merge-effects creature (effects+magnitude->effects effects magnitude))))
         creature
         env-effects)]
    [(-> encounter
         (assoc env-key env-effects*)
         (assoc-creature creature creature*))
     creature*]))

(s/fdef env-effects-tick
  :args (s/cat :encounter ::encounter
               :creature ::d/creature)
  :ret (s/tuple ::encounter ::d/creature))

(defn turn-effects-tick [{:keys [effects] :as creature}]
  (cond->
   (->> d/diminishing-effects
        (filter #(contains? effects %))
        (reduce
         (fn [creature effect]
           (let [magnitude (get effects effect)
                 magnitude (if (pos-int? magnitude) (dec magnitude) (inc magnitude))]
             (if (zero? magnitude)
               (update creature :effects dissoc effect)
               (assoc-in creature [:effects effect] magnitude))))
         creature))
    (contains? effects :burning) (update :health - 3)
    (contains? effects :bleeding) (update :health - 3)
    (contains? effects :mending) (update :health + (:mending effects))
    (contains? effects :poisoned) (update :health - (:poisoned effects))
    (contains? effects :marked) (->
                                 (update :health - (:marked effects))
                                 (update :effects dissoc :marked))
    :also (update :health max 0)))

(s/fdef turn-effects-tick
  :args (s/cat :creature ::d/creature)
  :ret ::d/creature)

(defn remove-dead-monsters [{:keys [monsters] :as encounter}]
  (let [dead-monsters (filter #(-> % :health zero?) monsters)]
    (-> encounter
        (update :monsters (partial remove #(u/contains-v? dead-monsters %)))
        (update :turn-order (partial remove #(u/contains-v? dead-monsters %))))))

(s/fdef remove-dead-monsters
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn victory? [{:keys [monsters]}]
  (empty? monsters))

(s/fdef victory?
  :args (s/cat :encounter ::encounter)
  :ret boolean?)

(defn defeat? [{:keys [kobolds]}]
  (every? #(= 0 (:health %)) kobolds))

(defn front-line-crumples? [creatures]
  (every? #(= :back (:row %)) creatures))

(s/fdef front-line-crumples?
  :args (s/cat :creatures (s/coll-of ::d/creature))
  :ret boolean?)

(defn crumple-front-line [encounter creatures]
  (reduce
   (fn [encounter creature]
     (update-creature encounter creature #(assoc % :row :front)))
   encounter
   creatures))

(s/fdef crumple-front-line
  :args (s/cat :encounter ::encounter
               :creatures (s/coll-of ::d/creature))
  :ret (s/coll-of ::d/creature))

(defn next-round [encounter]
  (let [{:keys [kobolds monsters] :as encounter*} (round-effects-tick encounter)
        [turn & turn-order] (c/get-turn-order kobolds monsters)]
    (-> encounter*
        (assoc :turn turn)
        (assoc :turn-order (vec turn-order))
        (update :round inc))))

(s/fdef next-round
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn next-turn [encounter]
  (let [[turn & turn-order] (:turn-order encounter)]
    (-> encounter
        (assoc :turn-order (vec turn-order))
        (assoc :turn turn))))

(s/fdef next-turn
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

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

(defn remove-dead-monsters [{:keys [monsters] :as encounter}]
  (let [dead-monsters (filter #(-> % :health zero?) monsters)]
    (reduce
     (fn [encounter monster]
       (->  encounter
            (update :monsters (partial remove #(= % monster)))
            (update :turn-order (partial remove #(= % monster)))))
     encounter
     dead-monsters)))

(s/fdef remove-dead-monsters
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn victory? [{:keys [monsters]}]
  (empty? monsters))

(s/fdef victory?
  :args (s/cat :encounter ::encounter)
  :ret boolean?)

(defn defeat? [{:keys [kobolds]}]
  (every? #(= 0 (:health %)) kobolds))

(s/fdef defeat?
  :args (s/cat :encounter ::encounter)
  :ret boolean?)

(defn front-line-crumples? [creatures]
  (every? #(= :back (:row %)) creatures))

(s/fdef front-line-crumples?
  :args (s/cat :creatures (s/coll-of ::d/creature))
  :ret boolean?)

(defn crumple-front-line [creatures]
  (map #(assoc % :row :front) creatures))

(s/fdef crumple-front-line
  :args (s/cat :creatures (s/coll-of ::d/creature))
  :ret (s/coll-of ::d/creature))

(defn next-round [{:keys [kobolds monsters] :as encounter}]
  (let [[turn & turn-order] (c/get-turn-order kobolds monsters)]
    (-> encounter
        (assoc :turn turn)
        (assoc :turn-order turn-order)
        (update :round inc)
        round-effects-tick)))

(s/fdef next-round
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn next-turn [encounter]
  (let [[turn & turn-order] (:turn-order encounter)]
    (-> encounter
        (assoc :turn-order turn-order)
        (assoc :turn turn))))
