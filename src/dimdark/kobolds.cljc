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
   {:prowess 3 :alacrity 3 :vigor 2 :spirit 4 :focus 5 :luck 1 :scales 2}
   :druid
   {:prowess 1 :alacrity 2 :vigor 3 :spirit 5 :focus 4 :luck 3 :stink 1 :squish 1}
   :ranger
   {:prowess 3 :alacrity 5 :vigor 2 :spirit 3 :focus 1 :luck 4 :stink 2}
   :warrior
   {:prowess 5 :alacrity 1 :vigor 4 :spirit 3 :focus 2 :luck 3 :squish 1 :brat 1}
   :sneak
   {:prowess 2 :alacrity 4 :vigor 2 :spirit 2 :focus 3 :luck 5 :brat 2}
   :guardian
   {:prowess 4 :alacrity 1 :vigor 5 :spirit 3 :focus 2 :luck 3 :scales 1 :squish 1}})

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

(s/def ::equipped (s/map-of ::eq/slot (s/nilable ::eq/equipment)))

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

(s/def ::all-abilities (s/coll-of (s/coll-of keyword? :count 3) :count 5))

(def class->abilities
  {:mage
   [[:dragon-blooded :firebolt :empower-spell]
    [:magic-missile :mage-armor :extend-spell]
    [:soaring-strike :flame-breath :blink]
    [:rend-and-tear :blood-rite :flame-wall]
    [:implode :devour :master-arcanist]]
   :druid
   [[:restore :tanglefoot :frostbolt]
    [:protect :swarm :rejuvenate]
    [:cleanse :skunkspray :nullify]
    [:healing-roots :hailstorm :snake-bite]
    [:true-strike :bury-alive :elder-druid]]
   :guardian
   [[:shield-wall :shield-bash :defensive-stance]
    [:armor-break :spirit-break :goad]
    [:dragon-heart :fire-slash :ice-slash]
    [:dragon-tail :giantslayer :radiant-stance]
    [:grim-cleave :dread-roar :juggernaut]]
   :warrior
   [[:weapon-master :precise-strike :shove]
    [:trip :disarm :wolf-stance]
    [:frostbite :sweep :knockdown]
    [:cold-snap :sunder :shatter]
    [:wintertide :eviscerate :lacerate]]
   :ranger
   [[:scout :poison-shot :jawtrap]
    [:aimed-shot :stinkbomb :field-medicine]
    [:toxic-rain :flashbang :camouflage]
    [:flurry-shot :hunters-mark :precision]
    [:trick-shot :exploding-shot :mawtrap]]
   :sneak
   [[:fade :backstab :feint]
    [:pickpocket :steal-magic :trapsense]
    [:skewer :hamstring :sabotage]
    [:brute :phase-bolt :evasion]
    [:barrel-roll :panache :arcane-trickster]]})

(def class->row
  {:mage :back
   :druid :back
   :ranger :back
   :sneak :front
   :warrior :front
   :guardian :front})

(def kobolds
  (reduce
   (fn [kobolds kobold-name]
     (let [klass (kobold-name kobold-name->class)
           growth (klass kobold-class-growth)
           attr-growth (into {} (filter #(contains? d/attributes (first %)) growth))
           merit-growth (into {} (filter #(contains? d/merits (first %)) growth))
           all-abilities (klass class->abilities)]
       (assoc kobolds kobold-name
              {:name kobold-name
               :level 1
               :class klass
               :abilities (set (first all-abilities))
               :all-abilities all-abilities
               :growth growth
               :row (klass class->row)
               :attributes (merge-with + base-attributes attr-growth)
               :merits merit-growth
               :equipped {:weapon (kobold-name starting-weapons)
                          :armor (kobold-name starting-armor)
                          :accessory nil}})))
   {}
   kobold-names))

(s/def ::kobold
  (s/with-gen
    (s/keys :req-un [::name
                     ::d/level
                     ::class
                     ::d/abilities
                     ::all-abilities
                     ::growth
                     ::d/row
                     ::d/attributes
                     ::d/merits
                     ::equipped])
    #(g/fmap
      (fn [kobold-name]
        (kobold-name kobolds))
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

(defn kobold->stats [{:keys [name attributes merits equipped row]}]
  (merge-with (fn [x y] (cond (number? x) (+ x y)
                              :else       (merge-with + x y)))
              (d/attributes+merits->stats attributes merits)
              (equipment-stats equipped)
              {:row row :name name}))

(s/fdef kobold->stats
  :args (s/cat :kobold ::kobold)
  :ret ::d/stats)

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

(defn kobold->creature [kobold]
  {:name (:name kobold)
   :stats (kobold->stats kobold)
   :effects {}
   :abilities (:abilities kobold)
   :row (:row kobold)
   :preferred-row (:row kobold)})

(s/fdef kobold->creature
  :args (s/cat :kobold ::kobold)
  :ret ::d/creature)
