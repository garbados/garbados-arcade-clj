(ns dimdark.abilities.mage)

(def abilities
  {:dragon-blooded
   {:name :dragon-blooded
    :description "The blood of great ones flows in your veins."
    :traits #{:passive}
    :affects {:aptitude 1 :scales 1}}
   :firebolt
   {:name :firebolt
    :description "Conjure a ball of oily fire and lob it at a foe."
    :traits #{:direct :ranged :hostile :fire :spell}
    :effects {:damage 0.5 :burning 1}}
   :empower-spell
   {:name :empower-spell
    :description "Focus your intent to strengthen your next spell."
    :traits #{:self :spell :mental}
    :effects {:empowered 1}}
   :magic-missile
   {:name :magic-missile
    :description "Fire bolts of unerring force to strike a foe."
    :traits #{:direct :piercing :hostile :spell :ranged}
    :effects {:damage 1}}
   :mage-armor
   {:name :mage-armor
    :description "Shield yourself with magic force."
    :traits #{:passive}
    :affects {:health 1 :armor 1}}
   :extend-spell
   {:name :extend-spell
    :description "Focus your intent to widen the effect of your next spell."
    :traits #{:self :spell :mental}
    :effects {:extended 1}}
   :soaring-strike
   {:name :soaring-strike
    :description "Spread your wings and strike from above!"
    :traits #{:hostile :direct :physical :piercing :ranged}
    :effects {:damage 1.5}
    :move-to :front}
   :flame-breath
   {:name :flame-breath
    :description "Exhale searing fire!"
    :traits #{:hostile :close :area :spell :fire}
    :effects {:damage 0.5 :burning 1 :scorched 1}}
   :blink
   {:name :blink ; TODO requires special handling
    :description "Teleport to the back row at the end of your turn."
    :traits #{:passive}}
   :rend-and-tear
   {:name :rend-and-tear
    :description "Lash out with lacerating claws!"
    :traits #{:hostile :close :direct :physical :fire}
    :effects {:damage 1 :bleeding 1}}
   :blood-rite
   {:name :blood-rite
    :description "Spill your own blood to invoke terrible power."
    :traits #{:self :spell :fire}
    :effects {:focused 2}
    :self-effects {:damage 0.5}}
   :flame-wall
   {:name :flame-wall
    :description "Spread standing flames across the enemy's back row."
    :traits #{:hostile :back-row :spell :fire}
    :effects {:damage 0.5 :burning 1}}
   :implode
   {:name :implode
    :description "Create and collapse a vacuum around an enemy."
    :traits #{:hostile :direct :spell :fire :ranged}
    :effects {:damage 2}}
   :devour
   {:name :devour
    :description "Gnaw an enemy's flesh, restoring yourself."
    :traits #{:hostile :direct :close :physical}
    :effects {:damage 1}
    :self-effects {:healing 1}}
   :master-arcanist
   {:name :master-arcanist
    :description "Having mastered the arcane arts, your powers reach untold heights."
    :traits #{:passive}
    :affects {:aptitude 2}}})
