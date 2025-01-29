(ns dimdark.core
  (:require
   [clojure.spec.alpha :as s]
   [clojure.walk :as walk]))

(s/def ::id keyword?)
(s/def ::name string?)
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
                   ::health ; NOTE THAT THIS IS MAX HEALTH
                   ::attack
                   ::defense
                   ::armor
                   ::initiative
                   ::fortune
                   ::aptitude
                   ::aptitudes
                   ::resistance
                   ::resistances]))

(s/def ::effect keyword?)
(s/def ::effects (s/map-of ::effect pos-int?))
(s/def ::environment (s/map-of ::row ::effects))

(s/def ::attr-or-merit
  (s/or :attr ::attribute
        :merit ::merit))
(s/def ::stat-or-merit
  (s/or :stat ::stat
        :merit ::merit))

(defn multiply-stats [stats n]
  (walk/postwalk
   (fn [x]
     (if (number? x)
       (* n x)
       x))
   stats))

(defn merge-stats [stats1 stats2]
  (merge-with #(if (map? %1)
                 (merge-with + %1 %2)
                 (+ %1 %2))
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
  {:health (* 3 (+ prowess vigor))
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

;; a creature is an instance of a being, prepped for combat!
(s/def ::creature
  (s/keys :req-un [::id
                   ::name
                   ::abilities
                   ::stats ; MAX HEALTH LIVES HERE
                   ::effects
                   ::row
                   ::health ; NOTE THAT THIS IS CURRENT HEALTH
                   ]))

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
