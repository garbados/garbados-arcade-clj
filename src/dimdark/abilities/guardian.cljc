(ns dimdark.abilities.guardian)

(def abilities
  {:shield-wall
   {:name :shield-wall
    :description "Deflect attacks meant for your comrades. Increases armor for allies."
    :traits #{:passive}
    :party-affects {:armor 1}}
   :shield-bash
   {:name :shield-bash
    :description "Slam your shield into an enemy, delaying their next turn."
    :traits #{:direct :close :hostile :physical}
    :uses {:armor 2}
    :effects {:damage 0.5 :quickened -1}}
   :defensive-stance
   {:name :defensive-stance
    :description "Steel your footing and prepare for battle!"
    :traits #{:self :spell :mental}
    :effects {:reinforced 0.5}}
   :armor-break
   {:name :armor-break
    :description "Cleave through an opponent's defenses!"
    :traits #{:direct :close :hostile :physical}
    :effects {:damage 0.5 :reinforced -1}}
   :spirit-break
   {:name :spirit-break
    :description "Devastate the enemy's morale with a terrifying strike."
    :traits #{:direct :close :hostile :physical}
    :effects {:damage 0.5 :blessed -1}}
   :goad
   {:name :goad
    :description "Taunt a foe into attacking you."
    :traits #{:close :direct :hostile :spell :mental}
    :effects {:taunted 1}}
   :dragon-heart
   {:name :dragon-heart
    :description "Draw from a spiritual reservoir to close your own wounds."
    :traits #{:self :spell :fire :mental}
    :effects {:mending 1}}
   :fire-slash
   {:name :fire-slash
    :description "Wreathe your blade in flame to singe a foe."
    :traits #{:hostile :close :direct :physical :fire}
    :effects {:damage 0.5 :scorched 1}}
   :ice-slash
   {:name :ice-slash
    :description "Enchant your blade with freezing influence."
    :traits #{:hostile :close :direct :physical :frost}
    :effects {:damage 0.5 :frozen 1}}
   :dragon-tail
   {:name :dragon-tail
    :description "Sweep the enemy line with a lash of the tail."
    :traits #{:hostile :close :area :physical :fire}
    :effects {:damage 1}}
   :giantslayer
   {:name :giantslayer
    :description "The bigger they are, the harder they fall."
    :traits #{:passive}
    :affects {:attack 2}}
   :radiant-stance
   {:name :radiant-stance
    :description "Summon the blessings of the bright hot center of the world."
    :traits #{:self :spell :mental}
    :effects {:blessed 1}}
   :grim-cleave
   {:name :grim-cleave
    :description "Disorganize foes with a wide and gruesome slash."
    :traits #{:hostile :area :close :physical :mental}
    :effects {:damage 0.5 :quickened -1}}
   :dread-roar
   {:name :dread-roar
    :description "A mighty shout strengthens the party."
    :traits #{:friendly :area :spell :mental}
    :effects {:sharpened 1}}
   :juggernaut
   {:name :juggernaut
    :description "Grow into an unstoppable force!"
    :traits #{:passive}
    :affects {:health 2 :armor 1 :resistance 2}}})
