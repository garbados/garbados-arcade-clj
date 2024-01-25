(ns dimdark.abilities.druid)

(def abilities
  {:restore
   {:name :restore
    :description "Invoke positive energy to close wounds."
    :traits #{:direct :friendly :spell :mental}
    :effects {:healing 2}}
   :tanglefoot
   {:name :tanglefoot
    :description "Roots sprout from the barren earth to grab at enemy feet."
    :traits #{:hostile :area :spell :poison :ranged}
    :effects {:quickened -1}}
   :frostbolt
   {:name :frostbolt
    :description "Hurl a frigid orb at a foe."
    :traits #{:hostile :direct :ranged :spell :frost}
    :effects {:damage 0.5 :chilled 1}}
   :protect
   {:name :protect
    :description "Summon a barrier of air and force to protect allies."
    :traits #{:friendly :area :spell :mental}
    :effects {:reinforced 1}}
   :swarm
   {:name :swarm
    :description "Afflict a foe with a buzzing horde of insects."
    :traits #{:hostile :ranged :direct :spell :poison}
    :effects {:blessed -1 :nauseous 1}}
   :rejuvenate
   {:name :rejuvenate
    :description "Enchant an ally to restore health over time."
    :traits #{:friendly :direct :spell :mental}
    :effects {:mending 2}}
   :cleanse
   {:name :cleanse
    :description "Purge negative effects from an ally."
    :traits #{:friendly :direct :spell :mental}
    :effects {:cleansed 1}}
   :skunkspray
   {:name :skunkspray
    :description "Douse your enemies in putrid gasses."
    :traits #{:hostile :ranged :front-row :spell :poison}
    :effects {:poisoned 1 :nauseous 1}}
   :nullify
   {:name :nullify
    :description "Purge positive effects from an enemy."
    :traits #{:hostile :ranged :direct :spell :mental}
    :effects {:purged 1}}
   :contain
   {:name :contain
    :description "Wrap a target in a brief but impenetrable bubble."
    :traits #{:ranged :direct :spell :mental}
    :effects {:contained 1}}
   :hailstorm
   {:name :hailstorm
    :description "Freezing ice and rain descend from above!"
    :traits #{:ranged :area :hostile :spell :frost}
    :effects {:damage 0.5 :frozen 1}}
   :snake-bite
   {:name :snake-bite
    :description "Loose your trusty viper upon a foe!"
    :traits #{:ranged :direct :hostile :piercing :spell :poison}
    :effects {:damage 0.5 :poisoned 1}}
   :augment
   {:name :augment
    :description "Hone an ally's weapon and will."
    :traits #{:friendly :direct :spell :mental}
    :effects {:sharpened 1}}
   :bury-alive
   {:name :bury-alive
    :description "Raise up the earth to swallow a foe!"
    :traits #{:hostile :direct :ranged :spell :piercing :mental}
    :effects {:damage 0.5 :quickened -1}}
   :elder-druid
   {:name :elder-druid
    :description "Your vast experience reveals the mysteries of nature."
    :traits #{:passive}
    :affects {:stink 2 :squish 2 :brat 2}}})
