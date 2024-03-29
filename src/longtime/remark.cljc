(ns longtime.remark 
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [longtime.core :as core]))

(def general-remarks
  ["Wild reeds sway on a river's edge."
   "Night stars glimmering grace the herd as it sleeps."
   "Herdlings frollick and laugh, making trouble wherever they can."])

(def spring-remarks
  ["Golden saffron blossoms fly in the verdant breeze."
   "The pale gloss of snow leaves the land to grow shining emerald."
   "The theater of another day unfurls with dawn's petrichor."])

(def summer-remarks
  ["Cloudless skies span wide above the herd."
   "A beating yellow sun gazes upon the land's cloven denizens."
   "Warmth wriggles into the fur of every minot, to varying delight and annoyance."])

(def fall-remarks
  ["A cool and cloudy day envelops the land."
   "Rain falls in sheets over sodden earth."
   "Hooves trudge through mud under bleak skies."])

(def winter-remarks
  ["Snow gathers on the wind, beating against fur and fabric."
   "Cold gathers, inspiring fur to bristle and teeth to chatter."])

(def season->remarks
  {0 spring-remarks
   1 summer-remarks
   2 fall-remarks
   3 winter-remarks})

(def plains-remarks
  ["Old willows sway on green hills, deep roots thick with seasons."
   "Stalks of wild grapplewheat shimmer in pale evening light."
   "A hawk swoops into the tall grass, emerging with a rat in its talons."])

(def forest-remarks
  ["Fungus-circles enchant the deep wood with mysterious patterns."
   "A mother-tree rises above the canopy, her trunk gnarled and ageless."
   "Curious deer watch passing minots from the safety of thick shade."
   "A lone bear relaxes on a bluff above the herd's camp, watching them idly."])

(def mountain-remarks
  ["Stone faces loom silent on a windless day."
   "Mountain peaks pierce the clouds, hiding behind their mists."
   "Birdsong echoes across cliffside valleys."])

(def jungle-remarks
  ["Air thick with life buzzes and twitters."
   "Complex birdcalls ring out, cut short by gnashing fang."])

(def swamp-remarks
  ["A long-forgotten log of bog butter emerges from the fetid damp."])

(def steppe-remarks
  ["Struggling grasses sweep over the broad terrain."
   "A big sky embraces the land, the vastness of eternity overhead."])

(def terrain->remarks
  {:plains plains-remarks
   :forest forest-remarks
   :mountain mountain-remarks
   :steppe steppe-remarks
   :swamp swamp-remarks
   :jungle jungle-remarks})

(defn gen-remarks
  [herd]
  (let [season (core/get-season herd)
        {terrain :terrain} (core/current-location herd)]
    (->> (concat general-remarks
                 (season->remarks season)
                 (terrain->remarks terrain))
         shuffle
         (take 2)
         (string/join " "))))

(s/fdef gen-remarks
  :args (s/cat :herd ::core/herd)
  :ret string?)
