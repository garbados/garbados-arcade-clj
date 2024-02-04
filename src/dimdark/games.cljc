(ns dimdark.games 
  (:require [clojure.spec.alpha :as s]
            [dimdark.equipment :as eq]
            [dimdark.combat :as c]
            [dimdark.core :as d]
            [dimdark.encounters :as e]
            [dimdark.kobolds :as k]
            [dimdark.quests :as q]
            [dimdark.monsters :as m]))

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
  {:salve #{:burning :scorched}
   :blanket #{:chilled :frozen}
   :antidote #{:poisoned :nauseous}
   :crystal #{:charmed}
   :bandages #{:bleeding}})
(s/def ::items
  (s/map-of ::item nat-int?))

(s/def ::escapade
  (s/or :event (s/keys :req-un [:event/name])
        :encounter ::e/encounter))

(defn init-monster-encounter
  ([kobolds level]
   (init-monster-encounter kobolds level (seq m/cultures)))
  ([kobolds level cultures]
   (let [monsters (reduce
                   (fn [monsters _]
                     (conj
                      monsters
                      (let [culture (rand-nth cultures)]
                        (m/gen-monster level culture))))
                   []
                   (range 4))
         turn-order (c/get-turn-order kobolds monsters)]
     {:kobolds kobolds
      :monsters monsters
      :kobolds-env {}
      :monsters-env {}
      :encounter {}
      :turn-order (drop 1 turn-order)
      :turn (first turn-order)
      :round 1})))

(defn init-playground-encounter
  [kobolds1 kobolds2]
  (let [turn-order (c/get-turn-order kobolds1 kobolds2)]
    {:kobolds kobolds1
     :monsters kobolds2
     :encounter {}
     :turn-order (drop 1 turn-order)
     :turn (first turn-order)
     :round 1}))

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
