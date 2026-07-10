(ns longtime.meta-text 
  (:require [arcade.core :refer-macros [inline-slurp]]
            [longtime.core :as core]
            [longtime.text :as text]
            [arcade.stache :as stache]))

(def intro-text (inline-slurp "resources/longtime/meta/introduction.txt"))
(def gameover-text (inline-slurp "resources/longtime/meta/gameover.txt"))
(def tutorial-text (inline-slurp "resources/longtime/meta/tutorial.mustache"))
(def projects-description (inline-slurp "resources/longtime/meta/projects.txt"))
(def individuals-description (inline-slurp "resources/longtime/meta/individuals.txt"))
(def credits-description (inline-slurp "resources/longtime/meta/credits.txt"))
(def path-description (inline-slurp "resources/longtime/meta/path.txt"))

(defn template-tutorial
  [herd]
  (stache/render tutorial-text {:spirit (:spirit herd)
                                :herd (:name herd)}))

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
