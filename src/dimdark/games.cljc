(ns dimdark.games 
  (:require
   [clojure.spec.alpha :as s]
   [dimdark.encounters :as e]
   [dimdark.equipment :as eq]
   [dimdark.kobolds :as k]
   [dimdark.quests :as q]))

(s/def ::name string?)
(s/def ::experience (s/int-in 0 151))
(s/def ::max-depth nat-int?)
(s/def ::equipment
  (s/coll-of ::eq/equipment))

(def items
  #{:potion
    :salve
    :antidote
    :bandages
    :blanket
    :crystal})
(s/def ::item items)

(def item-clears-effects
  {:salve :fire
   :blanket :frost
   :antidote :poison
   :crystal :mental
   :bandages :physical})

(s/def ::items
  (s/map-of ::item nat-int?))

(s/def ::escapade
  (s/or :event (s/keys :req-un [:event/name])
        :encounter ::e/encounter))

(s/def :delve/level nat-int?)

(s/def ::delve-adventure
  (s/keys :req-un [::escapade
                   :delve/level]))

(s/def :quest/stage (s/int-in 0 11))

(s/def ::quest-adventure
  (s/keys :req-un [::escapade
                   ::q/quest
                   :quest/stage]))

(s/def ::adventure
  (s/nilable
   (s/or :delve ::delve-adventure
         :quest ::quest-adventure)))

(s/def ::essence nat-int?)
(s/def ::game
  (s/keys :req-un [::name
                   ::k/kobolds
                   ::q/relics
                   ::experience
                   ::max-depth
                   ::equipment
                   ::items
                   ::essence
                   ::adventure]))

(defn init-new-game [name]
  {:name name
   :kobolds k/kobolds
   :relics #{}
   :experience 0
   :max-depth 0
   :equipment []
   :items []
   :essence 0
   :adventure nil})

(s/fdef init-new-game
  :args (s/cat :name string?)
  :ret ::game)
