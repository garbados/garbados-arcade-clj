(ns dimdark.games 
  (:require [clojure.spec.alpha :as s]
            [dimdark.equipment :as eq]
            [dimdark.kobolds :as k]))

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
  (s/keys :req-un [::k/kobolds
                   ::equipment
                   ::items
                   ::adventure
                   ::escapade]))

(defn init-new-game []
  {:kobolds k/kobolds
   :equipment []
   :items []
   :adventure nil
   :escapade nil})
