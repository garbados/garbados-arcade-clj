(ns dimdark.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::class keyword?)
(def rows #{:front :back})
(s/def ::row rows)
(s/def ::level (s/int-in 1 6))
(def attributes #{:prowess :alacrity :vigor :spirit :focus :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute pos-int?))
(def elements #{:fire :frost :poison :mental})
(s/def ::element elements)
(def merits #{:scales :squish :stink :brat})
(def merit->element
  {:scales :fire
   :squish :frost
   :stink :poison
   :brat :mental})
(s/def ::merit merits)
(s/def ::merits (s/map-of ::merit nat-int?))
(s/def ::abilities (s/coll-of keyword?))

(def ordered-stats
  [:health
   :attack
   :defense
   :armor
   :initiative
   :fortune
   :aptitude
   :resistance
   :fire-aptitude
   :fire-resistance
   :frost-aptitude
   :frost-resistance
   :poison-aptitude
   :poison-resistance
   :mental-aptitude
   :mental-resistance])
(def stats (set ordered-stats))
(s/def ::stat stats)
(s/def ::health nat-int?)
(s/def ::attack nat-int?)
(s/def ::defense nat-int?)
(s/def ::armor nat-int?)
(s/def ::initiative nat-int?)
(s/def ::fortune nat-int?)
(s/def ::aptitude nat-int?)
(s/def ::resistance nat-int?)
(s/def ::aptitudes (s/map-of ::element nat-int?))
(s/def ::resistances (s/map-of ::element nat-int?))
(s/def ::stats
  (s/keys :req-un [::health
                   ::attack
                   ::defense
                   ::armor
                   ::initiative
                   ::fortune
                   ::aptitude
                   ::aptitudes
                   ::resistance
                   ::resistances]))

(def effects
  #{:damage
    :healing
    :mending
    :delayed
    :bleeding
    :poisoned
    :nauseous
    :burning
    :scorched
    :chilled
    :frozen
    :sharpened
    :disarmed
    :focused
    :distracted
    :reinforced
    :exposed
    :blessed
    :cursed
    :quickened
    :slowed
    :laden
    :robbed})
(s/def ::effect effects)
(s/def ::effects (s/map-of ::effect pos-int?))

(s/def ::creature
  (s/keys :req-un [::name
                   ::level
                   ::class
                   ::row
                   ::attributes
                   ::merits
                   ::abilities]
          :opt-un [::stats
                   ::effects]))

(s/def ::attr-or-merit
  (s/or :attr ::attribute
        :merit ::merit))
(s/def ::stat-or-merit
  (s/or :stat ::stat
        :merit ::merit))
(defmulti creature-stat (fn [stat _] stat))
(s/fdef creature-stat
  :args (s/cat :stat ::stat-or-merit
               :creature ::creature)
  :ret nat-int?)
(defmethod creature-stat :health [_ creature]
  (* 3
     (+ (get-in creature [:attributes :prowess] 0)
        (get-in creature [:attributes :vigor] 0))))
(defmethod creature-stat :attack [_ creature]
  (get-in creature [:attributes :prowess] 0))
(defmethod creature-stat :defense [_ creature]
  (get-in creature [:attributes :alacrity] 0))
(defmethod creature-stat :armor [_ _]
  0)
(defmethod creature-stat :initiative [_ creature]
  (+ (get-in creature [:attributes :alacrity] 0)
     (get-in creature [:attributes :vigor] 0)))
(defmethod creature-stat :fortune [_ _]
  0)
(defmethod creature-stat :aptitude [_ creature]
  (get-in creature [:attributes :focus] 0))
(defmethod creature-stat :fire-aptitude [_ creature]
  (get-in creature [:aptitudes :fire] 0))
(defmethod creature-stat :frost-aptitude [_ creature]
  (get-in creature [:aptitudes :frost] 0))
(defmethod creature-stat :poison-aptitude [_ creature]
  (get-in creature [:aptitudes :poison] 0))
(defmethod creature-stat :mental-aptitude [_ creature]
  (get-in creature [:aptitudes :mental] 0))
(defmethod creature-stat :resistance [_ creature]
  (get-in creature [:attributes :spirit] 0))
(defmethod creature-stat :fire-resistance [_ creature]
  (get-in creature [:resistances :fire] 0))
(defmethod creature-stat :frost-resistance [_ creature]
  (get-in creature [:resistances :frost] 0))
(defmethod creature-stat :poison-resistance [_ creature]
  (get-in creature [:resistances :poison] 0))
(defmethod creature-stat :mental-resistance [_ creature]
  (get-in creature [:resistances :mental] 0))
(defmethod creature-stat :scales [_ creature]
  (get-in creature [:merits :scales] 0))
(defmethod creature-stat :squish [_ creature]
  (get-in creature [:merits :squish] 0))
(defmethod creature-stat :stink [_ creature]
  (get-in creature [:merits :stink] 0))
(defmethod creature-stat :brat [_ creature]
  (get-in creature [:merits :brat] 0))
(defn creature->stats [creature]
  (->> stats
       (map #(vec [% (creature-stat % creature)]))
       (reduce #(assoc %1 (first %2) (second %2)) {})))
(s/fdef creature->stats
  :args (s/cat :creature ::creature)
  :ret (s/map-of ::stat nat-int?))

(defn reset-creature [creature]
  (assoc creature
         :stats (creature->stats creature)
         :effects {}))

(s/fdef reset-creature
  :args (s/cat :creature ::creature)
  :ret ::creature)

(defn attributes->stats
  [{:keys [prowess alacrity vigor spirit focus luck
           scales squish stink brat]
    :or {prowess 0
         alacrity 0
         vigor 0
         spirit 0
         focus 0
         scales 0
         squish 0
         stink 0
         brat 0}}]
  {:health (+ prowess vigor)
   :attack prowess
   :defense alacrity
   :armor 0
   :initiative (+ alacrity vigor)
   :aptitude focus
   :aptitudes {:fire scales :frost squish :poison stink :mental brat}
   :resistance spirit
   :resistances {:fire scales :frost squish :poison stink :mental brat}
   :fortune luck})

(defn stats+effects=>stats
  [{:keys [health attack defense armor initiative aptitude aptitudes resistance resistances fortune]}
   {:keys [sharpened quickened reinforced blessed focused laden
           scorched chilled nauseous charmed]
    :or {sharpened 0
         quickened 0
         reinforced 0
         blessed 0
         focused 0
         laden 0
         scorched 0
         chilled 0
         nauseous 0
         charmed 0}}]
  {:health health
   :attack (+ attack sharpened)
   :defense (+ defense quickened)
   :armor (+ armor reinforced)
   :initiative (+ initiative quickened)
   :aptitude (+ aptitude focused)
   :aptitudes aptitudes
   :resistance (+ resistance blessed)
   :resistances (let [{:keys [fire frost poison mental]} resistances]
                  {:fire (- fire scorched)
                   :frost (- frost chilled)
                   :poison (- poison nauseous)
                   :brat (- mental charmed)})
   :fortune (+ fortune laden)})

(s/fdef stats+effects=>stats
  :args (s/cat :stats ::stats
               :effects ::effects)
  :ret ::stats)
