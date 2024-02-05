(ns dimdark.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(def rows #{:front :back})
(s/def ::row rows)
(s/def ::level (s/int-in 1 6))
(def attributes #{:prowess :alacrity :vigor :spirit :focus :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute pos-int?))
(def elements #{:fire :frost :poison :mental})
(s/def ::element elements)
(def merits #{:scales :squish :stink :brat})
(def element->merit
  {:fire :scales
   :frost :squish
   :poison :stink
   :mental :brat})
(s/def ::merit merits)
(s/def ::merits (s/map-of ::merit nat-int?))
(s/def ::abilities (s/coll-of keyword? :kind set?))

(s/def ::health nat-int?)
(s/def ::attack int?)
(s/def ::defense int?)
(s/def ::armor int?)
(s/def ::initiative int?)
(s/def ::fortune int?)
(s/def ::aptitude int?)
(s/def ::resistance int?)
(s/def ::aptitudes (s/map-of ::element int?))
(s/def ::resistances (s/map-of ::element int?))
(s/def ::stat #{:health :attack :defense :armor :initiative :aptitude :resistance :fortune
                :aptitudes :resistances})

(s/def ::stats
  (s/keys :opt-un [::row
                   ::health
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
    :hidden
    :pushed
    :pulled
    :empowered
    :extended
    :cleansed
    :purged
    :marked
    :delayed
    :bleeding
    :poisoned
    :nauseous
    :burning
    :scorched
    :chilled
    :frozen
    :charmed
    :sharpened
    :focused
    :reinforced
    :blessed
    :quickened
    :laden})
(s/def ::effect effects)
(s/def ::effects (s/map-of ::effect pos-int?))

(def diminishing-effects
  #{:mending
    :bleeding
    :poisoned
    :nauseous
    :burning
    :scorched
    :chilled
    :frozen
    :charmed
    :sharpened
    :focused
    :reinforced
    :blessed
    :quickened
    :laden})

(def stat-effects
  #{:sharpened
    :focused
    :reinforced
    :blessed
    :quickened
    :laden})

(def positive-effects
  #{:mending
    :hidden
    :empowered
    :extended})

(def negative-effects
  #{:marked
    :delayed
    :bleeding
    :poisoned
    :nauseous
    :burning
    :scorched
    :chilled
    :frozen
    :charmed})

(def env-effects
  #{:jawtrapped
    :mawtrapped})
(s/def ::env-effect env-effects)
(s/def ::environment (s/map-of ::env-effect pos-int?))

;; when an effect is negative, it uses a name deref'd from here
(def inverted-effects
  {:sharpened :disarmed
   :reinforced :exposed
   :quickened :slowed
   :focused :distracted
   :blessed :cursed
   :laden :robbed})

(s/def ::attr-or-merit
  (s/or :attr ::attribute
        :merit ::merit))
(s/def ::stat-or-merit
  (s/or :stat ::stat
        :merit ::merit))

(defn merge-stats [stats1 stats2]
  (merge-with (fn [x1 x2] (cond (map? x1) (merge-with + x1 x2)
                                :else     (+ x1 x2)))
              stats1
              stats2))

(defn attributes+merits->stats
  [{:keys [prowess alacrity vigor spirit focus luck]
    :or {prowess 0
         alacrity 0
         vigor 0
         spirit 0
         focus 0
         luck 0}}
   {:keys [scales squish stink brat]
    :or {scales 0
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

(s/fdef attributes+merits->stats
  :args (s/cat :attributes ::attributes
               :merits ::merits)
  :ret ::stats)

(defn stats+effects->stats
  [{:keys [health attack defense armor initiative aptitude aptitudes resistance resistances fortune]
    :or {health 0
         attack 0
         defense 0
         armor 0
         initiative 0
         aptitude 0
         aptitudes {}
         resistance 0
         resistances {}
         fortune 0}
    :as stats}
   {:keys [sharpened quickened reinforced blessed focused laden
           scorched chilled frozen nauseous charmed
           delayed]
    :or {sharpened 0
         quickened 0
         reinforced 0
         blessed 0
         focused 0
         laden 0
         scorched 0
         chilled 0
         frozen 0
         nauseous 0
         charmed 0
         delayed 0}}]
  (merge stats
         {:health health
          :attack (+ attack sharpened (- charmed))
          :defense (+ defense quickened (- nauseous) (- frozen))
          :armor (+ armor reinforced (- frozen))
          :initiative (+ initiative quickened (- chilled) (- delayed))
          :aptitude (+ aptitude focused (- scorched))
          :aptitudes aptitudes
          :resistance (+ resistance blessed)
          :resistances (let [{:keys [fire frost poison mental]
                              :or {fire 0 frost 0 poison 0 mental 0}} resistances]
                         {:fire (- fire scorched)
                          :frost (- frost chilled)
                          :poison (- poison nauseous)
                          :mental (- mental charmed)})
          :fortune (+ fortune laden)}))

(s/fdef stats+effects->stats
  :args (s/cat :stats ::stats
               :effects ::effects)
  :ret ::stats)

;; a creature is an instance of a being, prepped for combat!
(s/def ::creature
  (s/keys :req-un [::name
                   ::abilities
                   ::stats
                   ::effects
                   ::row
                   ::health]))

(s/def ::growth (s/map-of ::attr-or-merit pos-int?))

(defn parse-growth [growth]
  (reduce
   (fn [[attrs* merits*] [attr n]]
     (for [[group possible] [[attrs* attributes]
                             [merits* merits]]]
       (cond
         (contains? group attr) (update group attr + n)
         (contains? possible attr) (assoc group attr n)
         :else group)))
   [{} {}]
   growth))

(s/fdef parse-growth
  :args (s/cat :growth ::growth)
  :ret (s/tuple ::attributes ::merits))
