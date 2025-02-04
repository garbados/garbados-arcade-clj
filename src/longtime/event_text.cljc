(ns longtime.event-text 
  (:require [clojure.string :as string]
            [longtime.core :as core]
            [arcade.text :as text]))

(def -trait->adj
  {:angry      "strong"
   :kind       "loving"
   :attentive  "detailed"
   :mystical   "mysterious"
   :optimistic "hopeful"
   :poet       "eloquent"})

(def -journeying-remarks
  ["camping in the woods"
   "stampeding with another herd"
   "seeing distant lands"])

(def -offshoot-reasons
  ["followed the trail"
   "augured the stars"
   "stumbled half-starved"])

(def crossed-paths
  (fn [herd [orgcow] & _]
    (let [other-herd-name (rand-nth core/herd-names)]
      (string/join
       " "
       ["It takes a fair bit of planning for herds to safely cross paths,"
        "as" (:name orgcow) "well knows."
        "Their coming and going changes the land;"
        "delicate landscapes turn quickly to stamped mush."
        "So today they pass nearby, and a festival is held between."
        "Apples and haysweets, honored effigies, music and dancing;"
        "A mutual celebration of life!"
        "The next morning," (:name orgcow) "clasps their counterpart's hand."
        "Perhaps" (:name herd) "and" other-herd-name
        "will meet again in their lifetimes."
        "Perhaps they will only know each other once more"
        "in the folds of eternity."]))))

(def depression-ends
  (fn [_ [sadcow healcow] & _]
    (let [therapy-adj
          (or (first (filter some? (map -trait->adj (:traits healcow))))
              "thoughtful")]
      (string/join
       " "
       [(:name sadcow) "has been talking with" (:name healcow) "lately."
        "Much has troubled the former,"
        "about their life, their experiences, their place."
        "It has sabotaged their ability to feel joy; to feel at all."
        "What once fueled their passions has grown mundane and meaningless."
        "But" (:name healcow) "has had " therapy-adj "words for them,"
        "and open ears for them to speak and be heard."
        "In sitting with these words," (:name healcow)
        "has come to a new comprehension."
        "Habits and routines evolve, slowly but surely, to satisfy one's opaque needs."
        "A novel peace blossoms over the mind's rough terrain,"
        "and old devotions return like a dawn, ray by sunborn ray."
        "Thanks are insufficient. A gift is made, and refused, and shared."
        "\"No gift is necessary. We do this for each other,\" "
        (:name healcow) "asserts, \"Now and always.\""]))))

(def fire!
  (fn [_ [eepycow] infra]
    (if (keyword? infra)
      (string/join
       " "
       [(:name eepycow) "awakens with a start from a frightening dream."
        "A great fire spirit slipped from their exhausted limbs"
        "and swept about the earthen-thatch dwelling."
        "It is not easy for fire to catch that moist matter;"
        "the spirit must have been motivated."
        "In the waking world," (:name eepycow) "witnesses the conflagration"
        "bake the hovel around them."
        "They rush out as the smoke thickens, and coughing they watch it spread."
        "A water brigade forms while sparks spread from roof to roof."
        "Most of the camp is saved, but the" (name infra)
        "emerges little more than ash and ruin."
        (:name eepycow) "finds the tipped-over oil lamp that started it all,"
        "that must still have been burning when they fell asleep."
        "They have been so tired lately, something deep in their bones dragging them down."
        "Perhaps it was that spirit, they wonder."
        "Perhaps now it is gone."
        "A lightness enters their step; a burden, flown away."])
      "")))

(defn gruxnis-attack!
  [_herd [victim ibba] & _]
  (text/join-text
   "A Grux'nis is a great beast,
   a glorious testament to the fecund murk
   of the Yuliak Range's swampy base.
   Tough and shiny scales,
   quick and vicious beak,
   and toesome flippers that flop on land
   but fly in water.
   They eat the bottom-feeders of the upland bogs,
   but can be drawn by chance down the riverfloes
   to the paths of the herds."
   (:name victim) "was attacked on the beach,"
   "where" (:name ibba) "fought it off with adroit hand and crushing arm."
   "The beast was only scared back into the water, but not to end its ways.
   So the threat remained."
   (:name ibba) "traveled out lonesome to riddle with other herds"
   "of how to pacify a Grux'nis.
    In their heart they could not accept killing the animal outright,
    though some argued easily for the necessity.
    Facile, they thought.
    But still they took a spear and went to kill the thing.
    They goaded it with caught fish,
    but recognized in its rampaging eye a worm --
    a worm that aggravates the temper,
    and elicits rampant violence,
    that can be driven out with a tincture.
    Deftly they fought with the beast, as the sun rose and fell,
    nipping it with a blade bathed in sleeping poison.
    Blow by blow, the Grux'nis staggered,
    until it felt, and slept!
    Asleep," (:name ibba) "could treat it."
   "It awoke bound but clear-eyed."
   (:name ibba) "would not take chances,"
   "not more than this chance of knowing a Grux'nis in peace,
    and though it struggled she watched it.
    It was nervous now more than hungry, more than vicious.
    So after a time they brought it to the river's edge and undid the bonds.
    It fled, disappearing into the river with all the sound of a single droplet.
    After that there were no more attacks.
    The relief at violence averted brought great celebration."
   "But" (:name ibba) "wondered still, where that creature"
   "would go now. Back to the upland bogs, perhaps?
    Or perhaps... to stay near,
    to protect the one that saved its mind."))

(def journeying-ends
  (fn [herd & _]
    (let [journeycow (first (:new-adults herd))
          spent-time (rand-nth -journeying-remarks)]
      (->> [(:name journeycow) "has come in from their journey."
            "A year ago, they set out from this very herd, to live apart from it"
            "and understand the absence of its warmth."
            "The growing adult spent that time" spent-time
            "but admits, after many glasses of mulled wine,"
            "that" (:name herd) "has the greener grass underhoof."]
           (string/join " ")))))

(def offshoot-joins
  (fn [herd & _]
    (let [reason (rand-nth -offshoot-reasons)]
      (string/join
       " "
       ["Herds crumble to bits. Scared and hungry people factionalize."
        "Even vast herds can splinter into bands of half a dozen,"
        "a flood of hooves that disappears like a ripple across the continent."
        "A fair few today have found" (:name herd) "and committed to its Longtime."
        "They have" reason "to the herd's embrace."
        "With fire and wine, the many dance and celebrate."
        "May this plenitude persist!"
        "May their faith be true."]))))

(def plague
  (fn [_ [individual] passed?]
    (string/join
     " "
     (if passed?
       ["An ill wind seizes the people."
        "Many fall sick; healers leap into action."
        "They draw from plentiful reserves"
        "of medicines and expertise,"
        "preserving the lives of those afflicted."
        "Slowly but surely, they recover,"
        "returning to the daily business of the herd"
        "bit by bit, gently, day by day."
        "A season of loss, averted!"]
       ["An ill wind seizes the people."
        "Many fall sick; healers leap into action,"
        "though they are too little. Too late."
        (:name individual) "is not strong enough."
        "For want of experts and remedies,"
        "they perish in simmering agony."
        "With hollow, shallow breath, last words slip out."
        "\"Live on,\" they beg, \"For me. For everyone.\""]))))

(def ration-rot
  (fn [_ _ sanitarian]
    (string/join
     " "
     (if sanitarian
       ["The wrong sort of bug got the scent of our stores,"
        "and they buggered right in there overnight."
        "What a mess."
        "But" (:name sanitarian) "spotted it right away."
        "First thing in the morning, they threw the lot in sacks"
        "and queued them for the pyre."
        "By then others were disinfecting the earthen-cellar"
        "with tenderfire and incense."
        "We've lost a fair few meals from the ordeal,"
        "but we'll live."
        "That was never in doubt."]
       ["The wrong sort of bug got the scent of our stores,"
        "and they buggered right in there overnight."
        "What a mess."
        "Novice medicinairs even distributed rations"
        "before we noticed what had happened."
        "The illness now spreading is only temporary,"
        "and never fatal. It's just pain and gas."
        "Still, the whole camp will smell"
        "like burnt grasshoppers"
        "all week,"
        "and a great many meals went to the pyre"
        "to stop the rot's spread."
        "But, we'll live."
        "That was never in doubt."]))))

(def wound-healed
  (fn [_ [sadcow healcow] & _]
    (string/join
     " "
     ["Disability is produced by inaccessibility."
      "An order which excludes by design, limits its own potential."
      "Such an order can prevail even silently,"
      "imagining no such barrier could exist."
      "It must be rooted out," (:name healcow) "knows."
      "Accommodations call for quality of craft and heartful vision."
      "Some wounds never really heal, mental or physical,"
      "but we can make the changes we need,"
      "as long as we devote the effort and expertise."
      "Only in so doing can we live up to our values."
      (:name sadcow) "offers a small thanks,"
      "but their eyes go to the horizon,"
      "the world open to them unfettered."])))

(def funeral
  (fn [_herd [mourncow] deadcow]
    (text/join-text
     "All living things return to soil.
      Life comes from life through death.
      That is the cycle.
      For the dead, the soil is turned over,
      so that they lie deep beneath it,
      and a sapling is placed over them.
      What remains to mark their passing
      is a mound in the earth
      that becomes a limb of the land."
     (:name deadcow) "was a friend of" (:name mourncow) ";"
     "they will try to remember them fondly,
      however bitterly their absence tastes.
      They leave a complex legacy, a hole in the tapestry of the herd
      that will take generations to scar over.
      A friend heaps dirt on a hole in the ground
      where their confidant will remain forever,
      until the pieces of them return,
      spoken by the tongues of the wind.
      Life goes on,
      differently.")))
