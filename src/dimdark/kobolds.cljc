(ns dimdark.kobolds
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [dimdark.core :as d]
            [dimdark.equipment :as eq]
            #?(:clj [arcade.text :refer [inline-slurp]]
               :cljs [arcade.text :refer-macros [inline-slurp]])
            [clojure.edn :as edn]))

(def kobold-names
  #{:drg
    :grp
    :knz
    :muu
    :yap
    :yip})
(s/def ::name kobold-names)

(def kobold-classes
  #{:mage
    :druid
    :ranger
    :warrior
    :sneak
    :guardian})
(s/def ::class kobold-classes)

(def base-attributes
  (reduce
   #(assoc %1 %2 2)
   {}
   d/attributes))

(def kobolds
  (reduce
   (fn [kobolds text]
     (let [{:keys [name class abilities proficiencies growth equipped row]} (edn/read-string text)
           [attributes merits] (d/parse-growth growth)
           kobold
           {:name name
            :level 1
            :class class
            :abilities (set (first abilities))
            :proficiencies proficiencies
            :all-abilities abilities
            :growth growth
            :row row
            :attributes (merge-with + attributes base-attributes)
            :merits merits
            :equipped
            (let [{:keys [weapon armor]} equipped]
              {:weapon (eq/gen-basic-equipment weapon)
               :armor (eq/gen-basic-equipment armor)
               :accessory nil})}]
       (assoc kobolds name kobold)))
   {}
   [(inline-slurp "resources/dimdark/kobolds/drg.edn")
    (inline-slurp "resources/dimdark/kobolds/grp.edn")
    (inline-slurp "resources/dimdark/kobolds/knz.edn")
    (inline-slurp "resources/dimdark/kobolds/muu.edn")
    (inline-slurp "resources/dimdark/kobolds/yap.edn")
    (inline-slurp "resources/dimdark/kobolds/yip.edn")]))

(s/def ::weapons (s/coll-of :weapon/type :kind set? :count 2))
(s/def ::armor (s/coll-of :armor/class :kind set? :min-count 1))
(s/def ::proficiencies
  (s/keys :req-un [::weapons
                   ::armor]))
(s/def ::equipped (s/map-of ::eq/slot (s/nilable ::eq/equipment)))

(s/def ::kobold
  (s/with-gen
    (s/keys :req-un [::name
                     ::d/level
                     ::class
                     ::d/abilities
                     ::all-abilities
                     ::proficiencies
                     ::d/growth
                     ::d/row
                     ::d/attributes
                     ::d/merits
                     ::equipped])
    #(g/fmap
      (fn [kobold-name] (get kobolds kobold-name))
      (s/gen ::name))))

(s/def ::kobolds
  (s/map-of ::name ::kobold))

(defn equipment-stats [equipped]
  (reduce
   #(d/merge-stats %1 (eq/equipment->stats %2))
   {}
   (filter some? (vals equipped))))

(s/fdef equipment-stats
  :args (s/cat :equipped ::equipped)
  :ret ::d/stats)

(defn kobold->stats [{:keys [attributes merits equipped row]}]
  (merge-with (fn [x y] (cond (number? x) (+ x y)
                              :else       (merge-with + x y)))
              (d/attributes+merits->stats attributes merits)
              (equipment-stats equipped)
              {:row row}))

(s/fdef kobold->stats
  :args (s/cat :kobold ::kobold)
  :ret ::d/stats)

(defn equippable? [{:keys [proficiencies]} {:keys [slot type]}]
  (case slot
    :weapon
    (contains? (:weapons proficiencies) type)
    :armor
    (contains? (:armor proficiencies) (type eq/armor->class))
    :accessory
    true))

(s/fdef equippable?
  :args (s/cat :kobold ::kobold
               :equipment ::eq/equipment)
  :ret boolean?)

(defn kobold? [thing]
  (s/valid? ::kobold thing))

(s/fdef kobold?
  :args (s/cat :thing (s/or :? any?
                            :kobold ::kobold))
  :ret boolean?)

(defn kobold->creature [{:keys [name abilities row] :as kobold}]
  (let [{:keys [health] :as stats} (kobold->stats kobold)]
    {:name name
     :id name
     :stats stats
     :effects {}
     :abilities abilities
     :health health
     :row row}))

(s/fdef kobold->creature
  :args (s/cat :kobold ::kobold)
  :ret ::d/creature)
