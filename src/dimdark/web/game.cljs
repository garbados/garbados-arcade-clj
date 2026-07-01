(ns dimdark.web.game
  (:require [dimdark.web.lair.core :refer [lair-view]]
            [dimdark.web.adventure :refer [adventure-view]]
            [reagent.core :as r]))

(defn game-view [-game]
  (if (some? (:adventure @-game))
    [adventure-view -game]
    [lair-view -game (r/atom :lair)]))
