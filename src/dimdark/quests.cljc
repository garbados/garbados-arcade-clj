(ns dimdark.quests 
  (:require [clojure.spec.alpha :as s]
            #?(:clj [arcade.text :refer [inline-slurp]]
               :cljs [arcade.text :refer-macros [inline-slurp]])))

(def relics
  #{:despots-crown
    :ring-of-binding
    :eyes-of-drizket
    :ashes-of-thysricht
    :map-of-abaddon
    :ani-jewel})

(s/def ::relic relics)
(s/def ::relics (s/coll-of ::relic :kind set?))

(def quests
  #{:the-despots-crown
    :the-dragon-tamer
    :whom-the-earth-adores
    :light-and-hate
    :til-death})

(s/def ::quest quests)

(defn quest? [thing]
  (contains? quests thing))

(s/fdef quest?
  :args (s/cat :? (s/or :any any?
                        :quest ::quest))
  :ret boolean?)

(s/def ::requires (s/nilable ::relic))
(s/def ::reward ::relic)
(s/def ::title string?)
(s/def ::intro string?)
(s/def ::finale string?)
(s/def ::quest-details
  (s/keys :req-un [::requires
                   ::reward
                   ::title
                   ::intro
                   ::finale]))

(def quest->details
  {:the-despots-crown
   {:requires nil
    :reward :despots-crown
    :title "The Despot's Crown"
    :intro (inline-slurp "resources/dimdark/quests/the_despots_crown/intro.txt")
    :finale (inline-slurp "resources/dimdark/quests/the_despots_crown/finale.txt")}
   :the-dragon-tamer
   {:requires :despots-crown
    :reward :ring-of-binding
    :title "The Dragon Tamer"
    :intro (inline-slurp "resources/dimdark/quests/the_dragon_tamer/intro.txt")
    :finale (inline-slurp "resources/dimdark/quests/the_dragon_tamer/finale.txt")}
   :whom-the-earth-adores
   {:requires :ring-of-binding
    :reward :eyes-of-drizket
    :title "Whom the Earth Adores"
    :intro (inline-slurp "resources/dimdark/quests/whom_the_earth_adores/intro.txt")
    :finale (inline-slurp "resources/dimdark/quests/whom_the_earth_adores/finale.txt")}
   :light-and-hate
   {:requires :eyes-of-drizket
    :reward :ashes-of-thysricht
    :title "Light and Hate"
    :intro (inline-slurp "resources/dimdark/quests/light_and_hate/intro.txt")
    :finale (inline-slurp "resources/dimdark/quests/light_and_hate/finale.txt")}
   :til-death
   {:requires :map-of-abaddon
    :reward :ani-jewel
    :title "'Til Death"
    :intro (inline-slurp "resources/dimdark/quests/til_death/intro.txt")
    :finale (inline-slurp "resources/dimdark/quests/til_death/finale.txt")}})
