(ns dimdark.effects 
  (:require
   #?(:clj [arcade.text :refer [inline-slurp]]
      :cljs [arcade.text :refer-macros [inline-slurp]])
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [dimdark.core :as d]
   [dimdark.dice :as dice]))

(def effect->details
  (merge
   (edn/read-string
    (inline-slurp
     "resources/dimdark/effects/core.edn"))
   (edn/read-string
    (inline-slurp
     "resources/dimdark/effects/environment.edn"))
   (edn/read-string
    (inline-slurp
     "resources/dimdark/effects/elemental.edn"))))

(def effects (keys effect->details))
(s/def ::effect effects)
(s/def ::effects (s/map-of ::effect pos-int?))
(s/def ::type #{:buff :debuff :neutral})
(s/def ::phase #{nil :instant :turn-begin :on-spellcast :on-target :turn-end})
(s/def ::diminishing boolean?)
(s/def ::hurt (s/or :expr keyword?
                    :n number?))
(s/def ::heal (s/or :expr keyword?
                    :n number?))
(s/def ::move-to ::d/row)
(s/def ::removes ::type)
(s/def ::effect-details
  (s/keys :req-un [::type
                   ::phase]
          :opt-un [::diminishing
                   ::hurt
                   ::heal
                   ::move-to
                   ::removes
                   ::d/stats]))

(defn resolve-hurt [creature {:keys [hurt]} magnitude]
  (cond
    (keyword? hurt)
    (let [[n m] (string/split (name hurt) #"d")]
      (dice/roll (+ n magnitude) m))
    (< 1 hurt)
    (int (* hurt (:health creature))) ; proportion of current health
    :else
    (* hurt magnitude)))

(s/fdef resolve-hurt
  :args (s/cat :creature ::d/creature
               :effect ::effect-details
               :magnitude pos-int?)
  :ret nat-int?)

(defn resolve-heal [{:keys [heal]} magnitude]
  (if (keyword? heal)
    (let [[n m] (map int (string/split (name heal) #"d"))]
      (dice/roll (+ n magnitude) m))
    (* heal magnitude)))

(s/fdef resolve-heal
  :args (s/cat :creature ::d/creature
               :effect ::effect-details
               :magnitude pos-int?)
  :ret nat-int?)

(defn apply-effect-to-creature [creature effect-details magnitude]
  (cond
    ;; pushing and pulling
    (contains? effect-details :move-to)
    (assoc creature :row (:move-to effect-details))
    ;; purging and cleaning
    (contains? effect-details :removes)
    (let [to-remove (:removes effect-details)]
      (update creature :effects
              (fn [effects]
                (reduce
                 (fn [effects effect-name]
                   (dissoc effects effect-name))
                 effects
                 (for [effect-name (keys effects)
                       :let [effect-type (:type (effect->details effect-name))]
                       :when (= effect-type to-remove)]
                   effect-name)))))
    ;; TODO inflicts -- effects that cause other effects
    ;; hurting
    (contains? effect-details :hurt)
    (let [hurt-amt (resolve-hurt creature (:hurt effect-details) magnitude)]
      (update creature :health #(max 0 (- % hurt-amt))))
    ;; healing
    (contains? effect-details :heal)
    (let [heal-amt (resolve-heal (:heal effect-details) magnitude)]
      (update creature :health #(min (get-in creature [:stats :health])
                                     (+ % heal-amt))))))

(defn apply-instant-effects [{:keys [effects] :as creature}]
  (reduce
   (fn [creature [effect-name magnitude]]
     (let [{phase :phase
            :as details} (effect->details effect-name)]
       (cond-> (apply-effect-to-creature creature details magnitude)
         (= :instant phase)
         (dissoc :effects effect-name))))
   creature
   effects))

(defn apply-effect-to-ability []
  'todo)

(defn stats+effects->stats
  "Combines a canonical stat block
   with the impact of some effects,
   returning a new stat block."
  [stats effects]
  (reduce
   d/merge-stats
   stats
   (for [[effect-name magnitude] effects
         :let [{stat-effect :stats} (effect->details effect-name)]
         :when stat-effect]
     (d/multiply-stats stat-effect magnitude))))

(s/fdef stats+effects->stats
  :args (s/cat :stats ::d/stats
               :effects ::effects)
  :ret ::d/stats)

(defn diminish-effects-on-creature [{:keys [effects] :as creature}]
  (reduce
   (fn [creature effect-name]
     (let [details (effect->details effect-name)]
       (if (true? (:diminishing details))
         (let [n (->> (get details :resists #{})
                      (map #(get-in creature [:resistances %] 0))
                      (reduce + 1))
               creature* (update-in creature [:effects effect-name] - n)]
           (if (>= 0 (get-in creature* [:effects effect-name]))
             (update creature* :effects dissoc effect-name)
             creature*))
         creature)))
   creature
   (keys effects)))

(defn merge-effect [magnitude magnitude*]
  (cond
    (nil? magnitude) magnitude*
    (and (pos-int? magnitude)
         (pos-int? magnitude*)) (max magnitude magnitude*)
    (and (neg-int? magnitude)
         (neg-int? magnitude*)) (min magnitude magnitude*)
    :else (+ magnitude magnitude*)))

(s/fdef merge-effect
  :args (s/cat :magnitude int?
               :magnitude* int?)
  :ret boolean?)

(defn merge-effects [creature effects]
  (reduce
   (fn [creature [effect-name magnitude]]
     (update-in creature [:effects effect-name] merge-effect magnitude))
   creature
   effects))

(s/fdef merge-effects
  :args (s/cat :creature ::d/creature
               :effects ::effects)
  :ret ::d/creature)

(defn calc-effects-outcomes [effects magnitude]1
  (into {} (for [[effect coefficient] effects
                 :let [impact (int (* coefficient magnitude))]
                 :when (pos-int? impact)]
             [effect impact])))

(s/fdef effects+magnitude->effects
  :args (s/cat :effects ::effects
               :magnitude pos-int?)
  :ret ::effects)

(defn effects-in-phase [effects phase]
  (for [[effect-name magnitude] effects
        :let [details (effect->details effect-name)]
        :when (= phase (:phase details))]
    [effect-name details magnitude]))
