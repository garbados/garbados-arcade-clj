(ns planetcall.units
  (:require [clojure.spec.alpha :as s]
            [clojure.set :refer [union]]
            [planetcall.geometry :as geo]))

(s/def ::cost int?)
(s/def ::description string?)

(s/def ::trait-detail (s/keys :req-un [::description ::cost]))
(def trait->details
  {:aerial
   {:description "Unit operates at high altitudes."
    :cost 0}
   :all-terrain
   {:description "Movement costs for unit are always 1 or less."
    :cost 2}
   :automated
   {:description "Cheap automotons require constant maintenance."
    :cost 2}
   :clean
   {:description "Unit has an upkeep cost of 1."
    :cost 3}
   :dampening
   {:description "Unit reduces impact of resolve in nearby confrontations."
    :cost 3}
   :heavy
   {:description (str "Massive complex ordinance cannot counterattack,"
                      "nor be borne by aerial chassis.")
    :cost -1}
   :hidden
   {:description (str "Unit is only visible to adjacent units"
                      "and nearby perceptive entities.")
    :cost 3}
   :frail
   {:description "Unit has reduced structural integrity."
    :cost -2}
   :fortified
   {:description "Unit has increased structural integrity."
    :cost 2}
   :motivated
   {:description "Unit has increased base resolve."
    :cost 1}
   :obscuring
   {:description "Unit renders nearby allied units hidden."
    :cost 3}
   :perceptive
   {:description "Unit can perceive nearby hidden units."
    :cost 2}
   :psi-field
   {:description "Unit renders nearby friendly units psychic."
    :cost 3}
   :psychic
   {:description (str "During confrontations with this unit,"
                      "the influence of arms is halved"
                      "while the influence of resolve is doubled"
                      "for both the defender and attacker.")
    :cost 2}
   :radioactive
   {:description "This unit leaves radiation behind when it is destroyed."
    :cost 0}
   :reach
   {:description (str "When this unit's stack comes under aerial assault,"
                      "this unit may act as a defender.")
    :cost 1}
   :sharpshooter
   {:description (str "During confrontations with this unit,"
                      "it gains strength from its opponent's speed.")
    :cost 1}
   :supporting
   {:description "Unit grants advantage to nearby units."
    :cost 2}
   :transport
   {:description (str "Unit is capable of transporting other units"
                      "such as a ship moving troops by sea."
                      "Units may board the transport,"
                      "and disembark to leave it."
                      "Transported units cannot defend,"
                      "And will die if the transport is killed.")
    :cost 1}
   :troll
   {:description "Unit heals fully if they end their turn on fungus."
    :cost 0}
   :trooper
   {:description "Unit has advantage against native fauna."
    :cost 1}})

(def traits (set (keys trait->details)))
(s/def ::trait traits)
(s/def ::traits (s/coll-of ::trait :kind set?))

(s/def ::cooldown pos-int?)
(s/def ::ability-detail (s/keys :req-un [::cost ::description]
                                :opt-un [::cooldown]))
(def ability->details
  {:travel
   {:description "Cross land spaces."
    :cost 0}
   :embark
   {:description "Cross aquatic spaces."
    :cost 0}
   :hover
   {:description "Cross any type of space at a constant cost."
    :cost 2}
   :intercept
   {:description "Counter aerial attacks within range."
    :cost 0}
   :attack
   {:description "Confront a unit within range."
    :cost 0}
   :bombard
   {:description "Bombard a unit stack within range."
    :cost 0}
   :strike
   {:description "Commence an aerial assault of a unit stack within range."
    :cost 0}
   :disable
   {:description "Stun an adjacent stack for one turn."
    :cost 2
    :cooldown 3}
   :subvert
   {:description "Take control of an enemy unit for one turn."
    :cost 3
    :cooldown 4}
   :psychout
   {:description (str "Raise a nearby friendly unit's resolve,"
                      "or lower a nearby enemy's.")
    :cost 2
    :cooldown 2}
   :settle
   {:description (str "Unit constructs a stockpile improvement"
                      "even without adjacency to a claimed space"
                      "by disbanding on an unclaimed space.")
    :cost 3}
   :strategize
   {:description (str "Strengthen another unit with wise tactics.")
    :cost 1
    :cooldown 2}})
(def abilities (set (keys ability->details)))
(s/def ::ability abilities)
(s/def ::abilities (s/coll-of ::ability :kind set?))

;; CONDITIONS
;; fixme: this doesn't belong in this file / this file is too big

(s/def ::validator (s/or :fn fn?
                         :keyword keyword?
                         :spec s/spec?))
(s/def ::condition (s/keys :req-un [::description]
                           :opt-un [::validator]))
(def condition->details
  {:stunned
   {:description (str "Unit cannot counterattack"
                      "and begins its turn with zero movement.")
    :validator pos-int?}
   :subverted
   {:description "A hostile faction has hijacked this unit!"
    :validator nat-int?}
   :intercepting
   {:description "This unit is preparing to intercept possible aerial assaults."
    :validator (s/coll-of ::geo/coord :kind set?)}
   :reinforced
   {:description "This unit has a diminishing advantage from recent reinforcements."
    :validator pos-int?}})
(def conditions (-> condition->details keys set))
(s/def ::condition conditions)
(s/def ::conditions (s/map-of ::condition any?))

;; LOADOUTS

(s/def ::arms pos-int?)
(def loadout->details
  {:firearms   {:arms 2 :ability :attack}
   :railguns   {:arms 4 :ability :attack}
   :magspears  {:arms 6 :ability :attack}
   :discharger {:arms 8 :ability :attack}
   :missilier  {:arms 3 :ability :bombard :traits #{:heavy}}
   :disruptor  {:arms 5 :ability :bombard :traits #{:heavy}}
   :starcaller {:arms 7 :ability :bombard :traits #{:heavy}}
   :hacker     {:arms 1 :ability :disable}
   :saboteur   {:arms 1 :ability :subvert}
   ;:arkhana  {:arms 0 :ability :electrify}
   :arbiter    {:arms 1 :ability :attack :traits #{:obscuring}}
   :psi-beacon {:arms 1 :ability :psychout :traits #{:psi-field}}
   :commander  {:arms 1 :ability :strategize :traits #{:supporting}}
   :settler    {:arms 1 :ability :settle}
   ;; CRITTER LOADOUTS
   :loper      {:arms 3 :ability :attack}
   :wormswarm  {:arms 2 :ability :attack :traits #{:psychic}}
   :razorbeak  {:arms 4 :ability :attack}
   :bloodgnat  {:arms 3 :ability :attack}
   :oathwyrm   {:arms 10 :ability :attack}
   :draconaut  {:arms 5 :ability :attack}
   :bowerholm  {:arms 8 :ability :attack}
   :curator    {:arms 3 :ability :attack}
   :troll      {:arms 5 :ability :attack}
   :hewer      {:arms 7 :ability :attack}})
(def loadouts (set (keys loadout->details)))
(s/def ::loadout loadouts)

(s/def ::loadout-detail
  (s/keys :req-un [::arms ::ability] :opt-un [::traits]))

;; CHASSIS

(s/def ::speed pos-int?)
(def chassis->details
  {:infantry  {:speed 2 :ability :travel}
   :speeder   {:speed 4 :ability :travel}
   :jaeger    {:speed 4 :ability :travel :traits #{:all-terrain
                                                   :fortified}}
   :foil      {:speed 4 :ability :embark}
   :cruiser   {:speed 6 :ability :embark :traits #{:transport}}
   :windrider {:speed 6 :ability :hover :traits #{:frail}}
   :hovertank {:speed 6 :ability :hover :traits #{:fortified}}
   :spikejet  {:speed 8 :ability :intercept :traits #{:aerial}}
   :neograv   {:speed 8 :ability :hover :traits #{:all-terrain}}
   ;; CRITTER CHASSIS
   :loper     {:speed 4 :ability :travel :traits #{:all-terrain}}
   :wormswarm {:speed 3 :ability :travel :traits #{:psychic}}
   :razorbeak {:speed 6 :ability :hover :traits #{:all-terrain}}
   :bloodgnat {:speed 6 :ability :hover :traits #{:frail}}
   :oathwyrm  {:speed 4 :ability :travel :traits #{:fortified}}
   :draconaut {:speed 6 :ability :embark :traits #{:psychic}}
   :bowerholm {:speed 4 :ability :embark :traits #{:fortified}}
   :curator   {:speed 3 :ability :travel :traits #{:automated}}
   :troll     {:speed 3 :ability :travel :traits #{:troll}}
   :hewer     {:speed 3 :ability :travel :traits #{:fortified :all-terrain}}
   })
(def chassis (set (keys chassis->details)))
(s/def ::chassis chassis)

(s/def ::chassis-detail
  (s/keys :req-un [::speed ::ability] :opt-un [::traits]))

;; MODS

(def mod->details
  {:optical-camo    {:traits #{:hidden}}
   :zero-trace      {:traits #{:clean}}
   :volunteer       {:traits #{:motivated}}
   :psi-training    {:traits #{:psychic}}
   :automaton       {:traits #{:automated}}
   :durasteel       {:traits #{:fortified}}
   :trooper         {:traits #{:trooper}}
   :carrier         {:traits #{:transport}}
   :fissile-engine  {:traits #{:radioactive}}
   :anti-materiel   {:traits #{:sharpshooter}}
   :neural-dampener {:traits #{:dampening}}
   :observer        {:traits #{:perceptive}}})
(def mods (-> mod->details keys set))
(s/def ::mod mods)
(s/def ::mods (s/coll-of ::mod :kind set? :max-count 2))
(s/def ::mod mods)
(s/def ::design (s/keys :req-un [::loadout ::chassis ::mods]))

(s/def ::mod-detail
  (s/keys :opt-un [::traits ::ability]))

(defn get-traits-cost [traits]
  {:pre [(every? #(s/valid? ::trait %) traits)]
   :post [#(s/valid? ::cost %)]}
  (->> traits
       (map #(-> % trait->details :cost))
       (reduce + 0)))

(defn get-ability-cost [ability]
  {:pre [(s/valid? ::ability ability)]
   :post [#(s/valid? ::cost %)]}
  (-> ability ability->details :cost))

(defn get-detail-cost [detail]
  {:pre [(s/valid? (s/or
                    :loadout ::loadout-detail
                    :chassis ::chassis-detail
                    :mod     ::mod-detail)
                   detail)]
   :post [#(s/valid? ::cost %)]}
  (+
   (if-let [traits (detail :traits)]
     (get-traits-cost traits)
     0)
   (if-let [ability (detail :ability)]
     (get-ability-cost ability)
     0)))


(defn get-design-abilities [design]
  {:pre [(s/valid? ::design design)]
   :post [#(s/valid? ::abilities %)]}
  (merge
   (reduce union
           (map #(-> % mod->details :ability (or #{}))
                (design :mods)))
   (-> design :loadout loadout->details :ability)
   (-> design :chassis chassis->details :ability)))

(defn get-design-traits [design]
  {:pre [(s/valid? ::design design)]
   :post [#(s/valid? ::traits %)]}
  (union
   (reduce union (map #(-> % mod->details :traits (or #{}))
                      (design :mods)))
   (or (-> design :loadout loadout->details :traits) #{})
   (or (-> design :chassis chassis->details :traits) #{})))

(defn design-has-trait? [design trait]
  {:pre [(s/valid? ::design design) (s/valid? ::trait trait)]}
  ((-> design get-design-traits set) trait))

(defn get-design-cost [design]
  {:pre [(s/valid? ::design design)]
   :post [#(s/valid? ::cost %)]}
  (let [loadout-detail (-> design :loadout loadout->details)
        chassis-detail (-> design :chassis chassis->details)
        mod-details    (map mod->details (design :mods))
        arms-cost      (* 2 (loadout-detail :arms))
        speed-cost     (chassis-detail :speed)
        details        (into [loadout-detail chassis-detail] mod-details)
        other-cost     (reduce + 0 (map get-detail-cost details))
        total-cost     (+ arms-cost speed-cost other-cost)]
    (if (design-has-trait? design :automated)
      (int (/ total-cost 2))
      total-cost)))

(defn get-design-upkeep [design]
  {:pre [(s/valid? ::design design)]
   :post [#(s/valid? ::upkeep %)]}
  (let [cost (get-design-cost design)
        coef (cond (design-has-trait? design :automated) 1/2
                   (design-has-trait? design :clean) 0
                   :else 1/6)]
    (max 1 (int (* cost coef)))))

(defn valid-design?
  "Various specific tests for designs which cannot be encapsulated in the spec."
  [design]
  {:pre [(s/valid? ::design design)]
   :post [boolean?]}
  (not (and (design-has-trait? design :aerial)
            (or
              ;; aerial units cannot bear heavy loadouts
             (design-has-trait? design :heavy)
              ;; aerial units can only strike, not attack or bombard
             (let [abilities (get-design-abilities design)]
               (or (contains? abilities :attack)
                   (contains? abilities :bombard)))))))

(s/def ::ok-design (s/and ::design valid-design?))

(def starting-designs
  [{:loadout :firearms
    :chassis :infantry
    :mods #{}}])

;; UNIT SPEC


(s/def ::cooldowns (s/map-of ::ability ::cooldown))
(s/def ::movement nat-int?)
(s/def ::resolve number?)
(s/def ::integrity int?)
(s/def ::max-integrity pos-int?)
(s/def ::upkeep pos-int?)
(s/def ::faction int?)
(s/def ::unit
  (s/keys :req-un [::design
                   ::integrity
                   ::max-integrity
                   ::arms
                   ::speed
                   ::movement
                   ::resolve
                   ::traits
                   ::abilities
                   ::conditions
                   ::cooldowns
                   ::upkeep
                   ::faction]))

(defn design->unit [design player]
  {:pre [(s/valid? ::ok-design design)
         (s/valid? int? player)]
   :post [#(s/valid? ::unit %)]}
  (let [abilities (get-design-abilities design)
        traits    (get-design-traits design)
        integrity (cond (traits :frail)     7
                        (traits :fortified) 13
                        :else               10)]
    {:design        design
     :integrity     integrity
     :max-integrity integrity
     :arms          (-> design :loadout loadout->details :arms)
     :speed         (-> design :chassis chassis->details :speed)
     :movement      0
     :resolve       1
     :traits        traits
     :abilities     abilities
     :conditions    {}
     :cooldowns     {}
     :upkeep        (get-design-upkeep design)
     :faction       player}))
