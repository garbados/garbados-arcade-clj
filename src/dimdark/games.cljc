(ns dimdark.games 
  (:require [clojure.spec.alpha :as s]
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
(def item->trait
  {:salve :fire
   :blanket :frost
   :antidote :poison
   :crystal :mental
   :bandages :physical})
(s/def ::items
  (s/map-of ::item nat-int?))

;; TODO
(s/def ::adventure (s/nilable any?))
(s/def ::escapade (s/nilable any?))

(s/def ::game
  (s/keys :req-un [::name
                   ::k/kobolds
                   ::q/relics
                   ::experience
                   ::max-depth
                   ::equipment
                   ::items
                   ::adventure
                   ::escapade]))

(defn init-new-game [name]
  {:name name
   :kobolds k/kobolds
   :relics #{}
   :experience 0
   :max-depth 0
   :equipment []
   :items []
   :adventure nil
   :escapade nil})

(s/fdef init-new-game
  :args (s/cat :name string?)
  :ret ::game)
