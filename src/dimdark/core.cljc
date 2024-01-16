(ns dimdark.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::class keyword?)
(s/def ::level (s/int-in 1 6))
(def attributes #{:prowess :alactrity :vigor :spirit :focus :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute pos-int?))
(s/def ::abilities (s/coll-of keyword?))
(s/def ::creature
  (s/keys :req-un [::name
                   ::level
                   ::class
                   ::attributes
                   ::abilities]))

(def stats
  #{:health
    :attack
    :defense
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
    :brat})
(s/def ::health nat-int?)
(s/def ::attack nat-int?)
(s/def ::defense nat-int?)
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

(defn creature-health [creature]
  (* 3
     (+ (get-in creature [:attributes :prowess])
        (get-in creature [:attributes :vigor]))))

(defn creature-attack [creature]
  (get-in creature [:attributes :prowess]))

(defn creature-defense [creature]
  (get-in creature [:attributes :alacrity]))

(def creature-armor (constantly 0))

(defn creature-initiative [creature]
  (+ (get-in creature [:attributes :alacrity])
     (get-in creature [:attributes :vigor])))

(def creature-fortune (constantly 0))
