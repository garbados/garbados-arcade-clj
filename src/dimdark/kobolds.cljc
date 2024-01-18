(ns dimdark.kobolds
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [dimdark.core :as d]
            [dimdark.equipment :as eq]))

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

(def kobold-name->class
  {:drg :mage
   :grp :druid
   :knz :ranger
   :muu :warrior
   :yap :sneak
   :yip :guardian})

;; kobold growth apportions 20 growth points per level
(def kobold-class-growth
  {:mage
   {:prowess 3
    :alacrity 3
    :vigor 2
    :spirit 4
    :focus 5
    :luck 1
    :scales 2}
   :druid
   {:prowess 1
    :alacrity 2
    :vigor 4
    :spirit 5
    :focus 3
    :luck 3
    :stink 1
    :squish 1}
   :ranger
   {:prowess 3
    :alacrity 5
    :vigor 2
    :spirit 3
    :focus 1
    :luck 4
    :stink 1
    :brat 1}
   :warrior
   {:prowess 5
    :alacrity 1
    :vigor 4
    :spirit 3
    :focus 2
    :luck 3
    :squish 1
    :brat 1}
   :sneak
   {:prowess 2
    :alacrity 4
    :vigor 2
    :spirit 2
    :focus 3
    :luck 5
    :brat 2}
   :guardian
   {:prowess 4
    :alacrity 1
    :vigor 5
    :spirit 3
    :focus 2
    :luck 3
    :scales 2}})

(s/def ::growth
  (s/with-gen
    (s/map-of ::d/attr-or-merit ::d/level)
    #(g/fmap
      (fn [klass]
        (klass kobold-class-growth))
      (s/gen ::class))))

(def base-attributes
  (reduce
   #(assoc %1 %2 2)
   {}
   d/attributes))

(def starting-weapons
  {:drg (eq/gen-basic-equipment :tome)
   :grp (eq/gen-basic-equipment :staff)
   :knz (eq/gen-basic-equipment :bow)
   :muu (eq/gen-basic-equipment :spear)
   :yap (eq/gen-basic-equipment :dagger)
   :yip (eq/gen-basic-equipment :sword)})

(def starting-armor
  {:drg (eq/gen-basic-equipment :padded)
   :grp (eq/gen-basic-equipment :hide)
   :knz (eq/gen-basic-equipment :leather)
   :muu (eq/gen-basic-equipment :chain)
   :yap (eq/gen-basic-equipment :leather)
   :yip (eq/gen-basic-equipment :splint)})

(def kobolds
  (reduce
   (fn [kobolds kobold-name]
     (let [klass (kobold-name kobold-name->class)
           growth (klass kobold-class-growth)
           attr-growth (into {} (filter #(contains? d/attributes (first %)) growth))
           merit-growth (into {} (filter #(contains? d/merits (first %)) growth))]
       (assoc kobolds kobold-name
              {:name kobold-name
               :level 1
               :class klass
               :abilities []
               :growth growth
               :attributes (merge-with + base-attributes attr-growth)
               :merits merit-growth
               :aptitudes (reduce #(assoc %1 %2 0) {} d/elements)
               :resistances (reduce #(assoc %1 %2 0) {} d/elements)
               :equipped {:weapon (kobold-name starting-weapons)
                          :armor (kobold-name starting-armor)}})))
   {}
   kobold-names))

(s/def ::kobold
  (s/with-gen
    (s/and
     ::d/creature
     (s/keys :req-un [::name
                      ::class
                      ::growth
                      ::equipped]))
    #(g/fmap
      (fn [kobold-name]
        (kobold-name kobolds))
      (s/gen ::name))))

(s/def ::kobolds
  (s/map-of ::name ::kobold))

;; this function runs often so cache it
(defn- -equipment-stats [equipped]
  (merge-with
   +
   (let [{:keys [type level]} (:weapon equipped)]
     (eq/weapon-level->stats type level))
   (let [{:keys [type level]} (:armor equipped)]
     (eq/armor-level->stats type level))
   (->> (vals equipped)
        (map eq/equipment->mod-stats)
        (apply merge-with +))))
(def ^:private -eq-stat-cache (atom []))
(defn equipment-stats [{:keys [equipped]}]
  (let [cache-stats
        #(let [result (-equipment-stats equipped)]
           (swap! -eq-stat-cache [equipped result])
           result)]
    (if-let [[prev-equipped prev-result] @-eq-stat-cache]
      (if (= equipped prev-equipped)
        prev-result
        (cache-stats))
      (cache-stats))))

(s/fdef equipment-stats
  :args (s/cat :kobold ::kobold)
  :ret (s/map-of ::d/stat-or-merit nat-int?))

(defn kobold-stat [stat kobold]
  (+ (d/creature-stat stat kobold)
     (stat (equipment-stats kobold) 0)))

(s/fdef kobold-stat
  :args (s/cat :stat ::d/stat-or-merit
               :kobold ::kobold)
  :ret nat-int?)

(defn kobold->stats [kobold]
  (->> d/stats
       (map #(vec [% (kobold-stat % kobold)]))
       (reduce #(assoc %1 (first %2) (second %2)) {})))

(s/fdef kobold->stats
  :args (s/cat :kobold ::kobold)
  :ret (s/map-of ::d/stat-or-merit nat-int?))

(def proficiencies
  {:drg {:weapons #{:tome :orb}
         :armor #{:light}}
   :grp {:weapons #{:staff :club}
         :armor #{:light :medium}}
   :knz {:weapons #{:bow :crossbow}
         :armor #{:light}}
   :muu {:weapons #{:spear :polearm}
         :armor #{:light :medium}}
   :yap {:weapons #{:dagger :claws}
         :armor #{:light}}
   :yip {:weapons #{:sword :axe}
         :armor #{:medium :heavy}}})

(defn equippable? [{:keys [name]} {:keys [slot type]}]
  (case slot
    :weapon
    (contains? (get-in proficiencies [name :weapons]) type)
    :armor
    (contains? (get-in proficiencies [name :armor]) (type eq/armor->class))
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
