(ns dimdark.encounters
  (:require
   [clojure.spec.alpha :as s]
   [clojure.spec.gen.alpha :as g]
   [dimdark.abilities :as a]
   [dimdark.core :as d]
   [dimdark.effects :as effects]
   [dimdark.impacts :as impacts]
   [dimdark.monsters :as m]))

;; SPECS

(s/def ::entities (s/map-of ::d/id ::impacts/entity-map))
(s/def ::environment (s/map-of ::team ::d/environment))
(s/def ::turn-order ::d/id)
(s/def ::turn ::d/id)
(s/def ::round pos-int?)
(s/def ::encounter
  (s/keys :req-un [::entities
                   ::environment
                   ::turn-order
                   ::turn
                   ::round]))

;; UTIL

(defn get-turn-order [creatures]
  (->> creatures
       (filter
        (fn [{:keys [health]}]
          (pos-int? health)))
       (sort-by
        (fn [{:keys [stats effects]}]
          (:initiative (effects/stats+effects->stats stats effects))))
       (map :id)))

(s/fdef get-turn-order
  :args (s/cat :encounter ::encounter)
  :ret (s/coll-of ::d/id))

(defn update-creature [encounter id f]
  (update-in encounter [:entities id :entity] f))

(s/def ::update-creature-fn
  (s/fspec :args (s/cat :creature ::d/creature)
           :ret ::d/creature))

(s/fdef update-creature
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :id ::d/id
                 :f ::update-creature-fn)
          #(g/fmap
            (fn [[encounter f]]
              (let [id (-> encounter :entities rand-nth :entity :id)]
                [encounter id f]))
            (s/gen (s/tuple ::encounter ::update-creature-fn))))
  :ret ::encounter)

(defn assoc-creature [encounter id creature]
  (assoc-in encounter [:entities id :entity] creature))

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

;; INIT

(defn init-encounter
  [kobolds monsters]
  (let [entities
        (reduce
         (fn [entities {{id :id} :entity :as entity-map}]
           (assoc entities id entity-map))
         {}
         (concat
          (map #(hash-map :team :kobolds :entity %) kobolds)
          (map #(hash-map :team :monsters :entity %) monsters)))
        turn-order (get-turn-order (concat kobolds monsters))]
    {:entities entities
     :environment {:kobolds {}
                   :monsters {}}
     :encounter {}
     :turn-order (drop 1 turn-order)
     :turn (first turn-order)
     :round 1}))

(s/fdef init-encounter
  :args (s/cat :kobolds (s/coll-of ::d/creature)
               :monsters (s/coll-of ::d/creature))
  :ret ::encounter)

(defn init-monster-encounter
  ([kobolds level]
   (init-monster-encounter kobolds level (seq m/cultures)))
  ([kobolds level cultures]
   (->> (range 4)
        (map (fn [_] (m/gen-monster level (rand-nth cultures))))
        (reduce conj [])
        (init-encounter kobolds))))

(s/fdef init-monster-encounter
  :args (s/cat :kobolds (s/coll-of ::d/creature)
               :level pos-int?
               :cultures (s/? (s/coll-of ::m/culture)))
  :ret ::encounter)

;; PHASES

(defn realize-round-effects
  "Apply any effects that impact the beginning of a round,
   after turn order is decided."
  [encounter]
  ;; FIXME there are currently no such effects.
  encounter)

(s/fdef realize-round-effects
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn expand-by-phase
  "Gather and expand effects grouped by phase."
  [encounter id phase]
  (let [{team :team {:keys [effects]} :entity} (get-in encounter [:entities id])
        env-effects (get-in encounter [:environment team] {})]
    {:environment (effects/effects-in-phase env-effects phase)
     :creature (effects/effects-in-phase effects phase)}))

(defn apply-effects-to-creature [creature effects]
  (reduce
   (fn [creature [effect-name details magnitude]]
     (cond-> creature
       (not (true? (:diminishing details)))
       (update :effects dissoc effect-name)
       :else
       (effects/apply-effect-to-creature details magnitude)))
   creature
   effects))

(defn clear-environment-effects [encounter team effects]
  (reduce
   (fn [encounter [event-name _1 _2]]
     (update-in encounter [:environment team] dissoc event-name))
   encounter
   effects))

(defn realize-turn-begin
  "Apply and clear any effects that impact the beginning of a creature's turn."
  [encounter
   id
   {environmental-effects :environment
    creature-effects :creature}]
  (let [{team :team creature :entity} (get-in encounter [:entities id])
        effects (concat creature-effects environmental-effects)]
    (-> (clear-environment-effects encounter team environmental-effects)
        (assoc-creature id (apply-effects-to-creature creature effects)))))

(defn realize-turn-end
  "Diminish and clear relevant effects at the end of a turn."
  [encounter id]
  (update-creature encounter id effects/diminish-effects-on-creature))

(defn realize-on-creature
  "Apply and clear encounter effects on a given creature,
   returning the modified encounter and the creature's effective stat block."
  [encounter
   id
   {environmental-effects :environment
    creature-effects :creature}]
  (let [{team :team creature :entity} (get-in encounter [:entities id])
        effects (concat creature-effects environmental-effects)]
    (-> (clear-environment-effects encounter team environmental-effects)
        (assoc-creature id (apply-effects-to-creature creature effects)))))

(defn realize-impacts
  "Apply impacts to creatures and the environment,
   and resolve instant effects."
  [encounter impacts]
  (reduce
   (fn [encounter [who effects]]
     (println who effects)
     (if (#{:monsters :kobolds} who)
       (update-in encounter [:environment who] effects/merge-effects effects)
       (update-creature
        encounter
        who
        #(-> (effects/merge-effects % effects)
             (effects/apply-instant-effects)))))
   encounter
   impacts))

(defn get-possible-targets
  [{entities :entities} id {:keys [traits]}]
  (let [{team :team {:keys [row]} :entity} (get entities id)
        {friendlies true hostiles false}
        (group-by #(= team (:team %)) (vals entities))
        possibilities
        (cond
          (or (contains? traits :self)
              (and (contains? traits :close) (= :back row))
              (and (contains? traits :ranged) (= :front row))
              (contains? traits :environmental)) []
          (contains? traits :hostile) (map (comp :id :entity) hostiles)
          (contains? traits :friendly) (map (comp :id :entity) friendlies))]
    (cond->> possibilities
      (or (contains? traits :close)
          (contains? traits :front-row)) (filter #(= :front (:row %)))
      (contains? traits :back-row) (filter #(= :back (:row %))))))

(s/fdef get-possible-targets
  :args (s/cat :encounter ::encounter
               :entity-id ::d/id
               :ability ::a/ability-details)
  :ret (s/coll-of ::d/creature))

(defn get-usable-abilities [encounter id]
  (let [creature (get-in encounter [:entities id :entity])]
    (for [ability (a/filter-active (:abilities creature))
          :let [ability-details (a/ability->details ability)
                targets (get-possible-targets encounter creature ability-details)]
          :when (or (not (a/needs-target? ability-details))
                    (seq targets))]
      ability)))

(s/fdef get-usable-abilities
  :args (s/cat :encounter ::encounter
               :creature ::d/creature))

(s/fdef get-possible-targets
  :args (s/with-gen
          (s/cat :encounter ::encounter
                 :creature ::d/creature
                 :ability ::a/ability-details)
          #(g/fmap
            (fn [encounter]
              (let [group (rand-nth [:kobolds :monsters])
                    creature (rand-nth (get encounter group))
                    ability (rand-nth (get-usable-abilities encounter creature))]
                [encounter creature ability]))
            (s/gen ::encounter))))

(defn remove-dead-monsters [{:keys [entities] :as encounter}]
  (reduce
   (fn [encounter {team :team {:keys [id health]} :entity}]
     (if (and (= :monsters team)
              (zero? health))
       (-> (update encounter :entities dissoc id)
           (update :turn-order (comp vec remove) #(= id %)))
       encounter))
   encounter
   entities))

(s/fdef remove-dead-monsters
  :args (s/cat :encounter ::encounter)
  :ret ::encounter)

(defn victory? [{:keys [entities]}]
  (empty? (filter #(= :monsters (:team %)) entities)))

(s/fdef victory?
  :args (s/cat :encounter ::encounter)
  :ret boolean?)

(defn defeat? [{:keys [entities]}]
  (empty?
   (filter #(and (= :kobolds (:team %))
                 (pos-int? (get-in % [:entity :health])))
           entities)))

(s/fdef defeat?
  :args (s/cat :encounter ::encounter)
  :ret boolean?)

(defn front-line-crumples? [creatures]
  (every? #(= :back (:row %)) creatures))

(s/fdef front-line-crumples?
  :args (s/cat :creatures (s/coll-of ::d/creature))
  :ret boolean?)

(defn crumple-front-line
  "Moves all given creatures to the front row,
   such as when the front line crumbles."
  [encounter creatures]
  (reduce
   (fn [encounter {:keys [id]}]
     (update-creature encounter id #(assoc % :row :front)))
   encounter
   creatures))

(s/fdef crumple-front-line
  :args (s/cat :encounter ::encounter
               :creatures (s/coll-of ::d/creature))
  :ret (s/coll-of ::d/creature))

(s/fdef resolve-instant-effects
  :args (s/cat :creature ::d/creature)
  :ret ::d/creature)

(defn next-round [{:keys [entities] :as encounter}]
  (let [[turn & turn-order] (get-turn-order (map :entity entities))]
    (-> encounter
        (assoc :turn turn)
        (assoc :turn-order turn-order)
        (update :round inc)
        realize-round-effects)))

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

(defn auto-turn
  "An example of a turn, without any logging or input."
  [encounter]
  (let [id (:turn encounter)
        turn-begin-effects (expand-by-phase encounter id :turn-begin)
        encounter (realize-turn-begin encounter id turn-begin-effects)]
    (when (pos-int? (get-in encounter [:entities id :entity :health]))
      (let [usable-abilities (get-usable-abilities encounter id)
            chosen-ability-details (a/ability->details (rand-nth usable-abilities))
            ;; select target(s)
            possible-targets (get-possible-targets encounter id chosen-ability-details)
            targets
            (if (a/needs-target? chosen-ability-details)
              [(rand-nth possible-targets)]
              possible-targets)
            ;; gather on-target impacts to explain later
            on-target-impacts
            (reduce
             (fn [effects target-id]
               (assoc effects target-id #(expand-by-phase encounter % :on-target)))
             {}
             targets)
            ;; realize on-target impacts prior to action
            encounter
            (reduce
             (fn [encounter target-id]
               (realize-on-creature encounter target-id (get on-target-impacts target-id)))
             encounter
             targets)
            ;; realize on-action impacts
            on-spellcast-impacts (expand-by-phase encounter id :on-spellcast)
            encounter (realize-on-creature encounter id on-spellcast-impacts)
            ;; calculate action impact
            impacts
            (impacts/calc-impacts
             (get-in encounter [:entities id])
             chosen-ability-details
             (map #(get-in encounter [:entities % :entity]) targets))
            ;; realize impacts
            encounter (realize-impacts encounter impacts)]
        encounter))))
