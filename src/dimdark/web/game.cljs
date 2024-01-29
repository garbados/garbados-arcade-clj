(ns dimdark.web.game
  (:require [dimdark.web.lair :refer [lair-view]]
            [dimdark.web.adventure :refer [adventure-view]]
            [reagent.core :as r]))

(defn color-text [color x]
  [:span {:style {:color color}} x])

(defn game-view [-game]
  (if (some? (:adventure @-game))
    [adventure-view -game]
    [lair-view -game (r/atom :lair)]))
