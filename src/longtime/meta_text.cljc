(ns longtime.meta-text 
  (:require [arcade.core :refer-macros [inline-slurp]]
            [clojure.string :as string]
            [longtime.core :as core]
            [longtime.text :as text]
            [clostache.parser :as mustache]))

(def intro-text (inline-slurp "resources/longtime/meta/introduction.txt"))
(def gameover-text (inline-slurp "resources/longtime/meta/gameover.txt"))

(def tutorial-text (inline-slurp "resources/longtime/meta/tutorial.mustache"))

(defn template-tutorial
  [spirit herdname]
  (mustache/render tutorial-text {:spirit spirit :herd herdname}))

(def projects-description (inline-slurp "resources/longtime/meta/projects.txt"))

(def individuals-description
  (text/join-text
   "Herds consist of individuals, who have passions, traits, fulfillment, and skills.
    Minots with more fulfillment are happier; unhappiness breeds weariness and depression."))

(def path-description
  (text/join-text
   "The herd's migration path consists of stages of up to four locations each.
    At the end of each month, you will select a location from the next stage
    to travel to.
    Steppe locations can be crossed without spending a month,
    though nor can you enact projects on steppes.
    Longer paths may support larger herds,
    though it takes significant organization
    to adjust a migration like that."))

(def credits-description
  (string/join
   "\n"
   [(text/join-text
     "The Longtime is a labor of love.
      I dreamed of being the subtle and decisive influence
      that united the hopes of a great collective,
      a maelstrom of effort across countless generations.
      I did not want to watch my pawns scurry,
      driven by inscrutible menus.
      I wanted to be the heart of their ambitions!
      So I made this game.")
    (text/join-text
     "Much is owed to the prior art of games like
      Rimworld, Kitten Game, Frostpunk, Dwarf Fortress, and Stellaris,
      but especial thanks go to my partner Lucia Brody
      for developing with me the whole universe of *The Shepherd*,
      of which Minots are only a part.")]))

(def syndicate-remarks
  {:athletics
   ["rigorous exertion"
    "strenuous feats"]
   :craftwork
   ["strange inventions"
    "curious designs"]
   :geology
   ["beautiful stonework"
    "earthen foresight"]
   :herbalism
   ["advanced greenlore"
    "keen pathfinding"]
   :medicine
   ["enlightening panaceas"
    "gourmet dining"]
   :organizing
   ["meticulous planning"
    "historical consideration"]})

(defn announce-syndicate [syndicate]
  (let [remarks (map syndicate-remarks syndicate)
        [r1 r2] (map rand-nth remarks)]
    (text/join-text
     "Record-keepers and rhetoricians rejoice!"
     "Enthusiasts have joined together in debate and duel."
     "They bicker and bother, sussing with susurrus"
     "the finer points of some greater ethos."
     (str "Through " r1 " and " r2 ",")
     "a potent consensus emerges,"
     "a bright and capable vision!"
     (str "So is founded " (core/syndicate-name syndicate) "."))))
