;; gather things for the repl
(ns planetcall.core
  (:require [planetcall.actions :as pa]
            [planetcall.ai.random :as rand-ai]
            [planetcall.games :as pg]
            [planetcall.gathering :refer [do-gather-phase]]
            [planetcall.geometry :refer [get-adjacent-coords]]
            [planetcall.ideotech :refer [can-research]]
            [planetcall.planet :as planet]
            [planetcall.radiation :as radiation]
            [planetcall.weather :as weather]
            [planetcall.abilities :as pb]))

(defn begin-turn [game]
  (-> game
      pg/diminish-world-conditions
      radiation/radiation-hurts
      radiation/radiation-spreads
      weather/miasma-moves
      planet/spawn-phase))

(defn end-turn [game]
  (pg/record-player-victories game))
