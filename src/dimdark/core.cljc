(ns dimdark.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::class keyword?)
(s/def ::level (s/int-in 1 6))
(def attributes #{:prowess :alacrity :vigor :spirit :focus :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute pos-int?))
(def elements #{:fire :frost :poison :mental})
(s/def ::element elements)
(s/def ::aptitudes (s/map-of ::element nat-int?))
(s/def ::resistances (s/map-of ::element nat-int?))
(def merits #{:scales :squish :stink :brat})
(def merit->element
  {:scales :fire
   :squish :frost
   :stink  :poison
   :brat   :mental})
(s/def ::merit merits)
(s/def ::merits (s/map-of ::merit nat-int?))
(s/def ::abilities (s/coll-of keyword?))
(s/def ::creature
  (s/keys :req-un [::name
                   ::level
                   ::class
                   ::attributes
                   ::aptitudes
                   ::resistances
                   ::merits
                   ::abilities]))

(def ordered-stats
  [:health
   :attack
   :defense
   :armor
   :initiative
   :fortune
   :aptitude
   :fire-aptitude
   :frost-aptitude
   :poison-aptitude
   :mental-aptitude
   :resistance
   :fire-resistance
   :frost-resistance
   :poison-resistance
   :mental-resistance
   :scales
   :squish
   :stink
   :brat])
(def stats (set ordered-stats))
(s/def ::stat stats)
(s/def ::health nat-int?)
(s/def ::attack nat-int?)
(s/def ::defense nat-int?)
(s/def ::armor nat-int?)
(s/def ::initiative nat-int?)
(s/def ::fortune nat-int?)
(s/def ::aptitude nat-int?)
(s/def ::fire-aptitude nat-int?)
(s/def ::frost-aptitude nat-int?)
(s/def ::poison-aptitude nat-int?)
(s/def ::mental-aptitude nat-int?)
(s/def ::resistance nat-int?)
(s/def ::fire-resistance nat-int?)
(s/def ::frost-resistance nat-int?)
(s/def ::poison-resistance nat-int?)
(s/def ::mental-resistance nat-int?)
(s/def ::scales nat-int?)
(s/def ::squish nat-int?)
(s/def ::stink nat-int?)
(s/def ::brat nat-int?)
(s/def ::stats
  (s/keys :req-un [::health
                   ::attack
                   ::defense
                   ::armor
                   ::initiative
                   ::fortune
                   ::aptitude
                   ::fire-aptitude
                   ::frost-aptitude
                   ::poison-aptitude
                   ::mental-aptitude
                   ::resistance
                   ::fire-resistance
                   ::frost-resistance
                   ::poison-resistance
                   ::mental-resistance
                   ::scales
                   ::squish
                   ::stink
                   ::brat]))

(defmulti creature-stat (fn [stat _] stat))
(s/fdef creature-stat
  :args (s/cat :stat ::stat
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
(defmethod creature-stat :scales [_ creature]
  (get-in creature [:merits :scales] 0))
(defmethod creature-stat :squish [_ creature]
  (get-in creature [:merits :squish] 0))
(defmethod creature-stat :stink [_ creature]
  (get-in creature [:merits :stink] 0))
(defmethod creature-stat :brat [_ creature]
  (get-in creature [:merits :brat] 0))
(defmethod creature-stat :aptitude [_ creature]
  (get-in creature [:attributes :focus] 0))
(defmethod creature-stat :fire-aptitude [_ creature]
  (+ (creature-stat :aptitude creature)
     (creature-stat :scales creature)
     (get-in creature [:aptitudes :fire] 0)))
(defmethod creature-stat :frost-aptitude [_ creature]
  (+ (creature-stat :aptitude creature)
     (creature-stat :squish creature)
     (get-in creature [:aptitudes :frost] 0)))
(defmethod creature-stat :poison-aptitude [_ creature]
  (+ (creature-stat :aptitude creature)
     (creature-stat :stink creature)
     (get-in creature [:aptitudes :poison] 0)))
(defmethod creature-stat :mental-aptitude [_ creature]
  (+ (creature-stat :aptitude creature)
     (creature-stat :brat creature)
     (get-in creature [:aptitudes :mental] 0)))
(defmethod creature-stat :resistance [_ creature]
  (get-in creature [:attributes :spirit] 0))
(defmethod creature-stat :fire-resistance [_ creature]
  (+ (creature-stat :resistance creature)
     (creature-stat :scales creature)
     (get-in creature [:resistances :fire] 0)))
(defmethod creature-stat :frost-resistance [_ creature]
  (+ (creature-stat :resistance creature)
     (creature-stat :squish creature)
     (get-in creature [:resistances :frost] 0)))
(defmethod creature-stat :poison-resistance [_ creature]
  (+ (creature-stat :resistance creature)
     (creature-stat :stink creature)
     (get-in creature [:resistances :poison] 0)))
(defmethod creature-stat :mental-resistance [_ creature]
  (+ (creature-stat :resistance creature)
     (creature-stat :brat creature)
     (get-in creature [:resistances :mental] 0)))
(defn creature->stats [creature]
  (->> stats
       (map #(vec [% (creature-stat % creature)]))
       (reduce #(assoc %1 (first %2) (second %2)) {})))
(s/fdef creature->stats
  :args (s/cat :creature ::creature)
  :ret (s/map-of ::stat nat-int?))
