(ns dimdark.abilities.ranger)

(def abilities
  {:scout ; TODO requires special handling
   {:name :scout
    :description "Keen eyes warn of danger before it's too late. Raises party initiative."
    :traits #{:passive}
    :party-affects {:initiative 2}}
   :poison-shot
   {:name :poison-shot
    :description "Loose an arrow coated in toxins."
    :traits #{:ranged :direct :hostile :physical :poison}
    :effects {:damage 0.5 :poisoned 1}}
   :jawtrap
   {:name :jawtrap
    :description "Lay a biting trap that the next enemy to act will trigger."
    :traits #{:ranged :hostile :environmental :spell}
    :env-effects {:jawtrapped 1}}
   :aimed-shot
   {:name :aimed-shot
    :description "Set your sights on an enemy's weak point."
    :traits #{:ranged :hostile :direct :piercing :physical}
    :uses {:aptitude 1}
    :effects {:marked 2}}
   :stinkbomb
   {:name :stinkbomb
    :description "Hurl a noxious explosive into the ranks of your foes."
    :traits #{:ranged :area :hostile :spell :poison}
    :effects {:nauseous 1}}
   :field-medicine
   {:name :field-medicine
    :description "Apply herbal remedies to mend the wounds of allies."
    :traits #{:friendly :direct :spell :poison}
    :effects {:mending 1}}
   })