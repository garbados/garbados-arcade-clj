(ns planetcall-next.rules.entities
   (:require
    [clojure.spec.alpha :as s]))

(s/def :geo/coord (s/tuple int? int?))

(s/def :unit/loadout keyword?)
(s/def :unit/chassis keyword?)
(s/def :unit/mods (s/coll-of keyword? :max-count 2))

(s/def :unit/design
  (s/keys :req-un [:unit/loadout
                   :unit/chassis
                   :unit/mods]))

(s/def :unit/integrity nat-int?)
(s/def :unit/max-integrity pos-int?)
(s/def :unit/arms pos-int?)
(s/def :unit/resolve pos-int?)
(s/def :unit/base-resolve pos?)
(s/def :unit/moves nat-int?)
(s/def :unit/max-moves pos-int?)
(s/def :unit/traits (s/coll-of keyword? :kind set?))
(s/def :unit/conditions (s/map-of keyword? any?))
(s/def :unit/faction :faction/i)

(s/def :ability/abilities (s/coll-of keyword? :kind set?))
(s/def :ability/cooldowns (s/map-of keyword? pos-int?))

(s/def :unit/id uuid?)

(s/def :unit/unit
  (s/keys :req-un [:unit/id
                   :unit/design
                   :unit/integrity
                   :unit/max-integrity
                   :unit/arms
                   :unit/resolve
                   :unit/base-resolve
                   :unit/moves
                   :unit/max-moves
                   :unit/traits
                   :unit/conditions
                   :unit/upkeep
                   :unit/faction
                   :ability/abilities
                   :ability/cooldowns]))

(s/def :faction/designs (s/coll-of :unit/design :kind set?))

(s/def :faction/resources
  (s/map-of #{:food :materials :energy :insight} nat-int?))
 
(s/def :research/current (s/nilable (s/tuple keyword? nat-int?)))
(s/def :research/known (s/coll-of keyword? :kind set?))
(s/def :research/experience (s/map-of keyword? nat-int?))
(s/def :faction/research
  (s/keys :req-un [:research/current
                   :research/known
                   :research/experience]))

(s/def :faction/conditions (s/map-of keyword? any?))

(s/def :faction/claimed (s/coll-of :geo/coord :kind set?))
(s/def :faction/visible (s/coll-of :geo/coord :kind set?))
(s/def :faction/seen (s/coll-of :geo/coord :kind set?))
(s/def :faction/i nat-int?)
(s/def :faction/name string?)

(s/def :game/faction
  (s/keys :req-un [:faction/i
                   :faction/name
                   :faction/designs
                   :faction/resources
                   :faction/research
                   :faction/conditions
                   :faction/claimed
                   :faction/visible
                   :faction/seen]))

(s/def :game/factions (s/coll-of :game/faction :kind vector?))

(s/def :game/wonders (s/map-of keyword? nat-int?))

(s/def :space/prefix keyword?)
(s/def :space/suffix keyword?)
(s/def :space/feature keyword?)
(s/def :space/improvement keyword?)
(s/def :space/controller :faction/i)
(s/def :space/fungus boolean?)
(s/def :space/miasma boolean?)
(s/def :space/road boolean?)

(s/def :space/space
  (s/keys :req-un [:space/prefix
                   :space/suffix
                   :space/feature
                   :space/improvement
                   :space/controller
                   :space/fungus
                   :space/miasma
                   :space/road
                   :geo/coord]))

(s/def :world/eco-damage nat-int?)
(s/def :world/conditions :faction/conditions)

(s/def :turn/n nat-int?)
(s/def :turn/actions nat-int?)
(s/def :turn/phase #{nil :actions :abilities :upkeep})

(s/def :game/spaces (s/map-of :geo/coord :space/space))
(s/def :game/units (s/map-of :unit/id :unit/unit))

(s/def :game/world
  (s/keys :req-un [:world/eco-damage
                   :world/conditions]))

(s/def :game/turn
  (s/keys :req-un [:turn/n
                   :turn/actions
                   :turn/phase]))

;;  -1 : vendetta
;;   0 : strangers
;;   1 : neutral
;;   2 : friends
;;   3 : allies
;;   4 : partners
(s/def :game/treaties (s/map-of (s/coll-of :faction/i
                                           :kind set?
                                           :count 2)
                                (s/int-in -1 5)))

(s/def :game/game
  (s/keys :req-un [:game/factions
                   :game/treaties
                   :game/wonders
                   :game/spaces
                   :game/units
                   :game/world
                   :game/turn]))
