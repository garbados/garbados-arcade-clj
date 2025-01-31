(ns dimdark.help)

(def explainers
  {:effects
   {:damage
    #(str %1 " suffered " %2 " damage!")
    :healing
    #(str %1 " gained " %2 " hit points!")
    :mending
    #(str "The wounds of " %1 " begin to mend, for " %2 " hit points each turn.")
    :hidden
    #(str "Melding into the shadows, " %1 " cannot be targeted until after their next action.")
    :pushed
    #(str % " has moved into the back row!")
    :pulled
    #(str % " has moved into the front row!")
    :empowered
    #(str "The next spell " %1 " casts will be " %2 " points more powerful!")
    :extended
    #(str "The next spell " %1 " casts will affect all valid targets!")
    :cleansed
    #(str %1 " has been cleansed of negative statuses!")
    :purged
    #(str %1 " has been purged of positive statuses!")
    :marked
    #(str %1 " will take " %2 " damage at the end of their next turn.")
    :delayed
    #(str %1 " has had their initiative delayed by " %2 " points.")
    :bleeding
    #(str %1 " has suffered a grevious wound that will inflict 3 damage for " %2 " turns.")
    :poisoned
    #(str %1 " has been poisoned, and will suffer " %2 " damage, one less each turn until it expires.")
    :nauseous
    #(str %1 " nearly vomits from nausea, lowering their defense and poison resistance by " %2 "!")
    :burning
    #(str %1 " has caught fire, and will suffer 3 damage for " %2 " turns.")
    :scorched
    #(str %1 " suffers scorching burns, lowering their aptitude and fire resistance by " %2 "!")
    :chilled
    #(str %1 " shivers with cold, lowering their initiative and cold resistance by " %2 "!")
    :frozen
    #(str %1 " nearly stops cold as ice envelops them, lowering their defense and armor by " %2 "!")
    :charmed
    #(str %1 " has been beguiled by their enemies, lowering their attack and mental resistance by " %2 "!")
    :sharpened
    #(str "A keen edge forms on the weapon of " %1 ", granting " %2 "attack!")
    :focused
    #(str "Absolute focus wells up within the mind of " %1 ", granting " %2 "aptitude!")
    :reinforced
    #(str "Invisible shields surround " %1 ", granting " %2 " armor!")
    :blessed
    #(str %1 " rises to withstand the elements, granting " %2 " resistance!")
    :quickened
    #(str "Winds push at the back of " %1 ", granting " %2 " initiative!")
    :laden
    #(str "The pockets of " %1 " bulge with loot.")}})
