(ns planetcall.wonders
  (:require [clojure.spec.alpha :as s]))

(def wonders #{:ark-launchpad
               :beacon-institute
               :dimensional-gate
               :earthscape
               :empath-guild
               :grand-reliquary
               :heavens-eye
               :planet-buster
               :planetary-congress
               :planetdream
               :singularity-collider
               :survivors-song})

(def transformational-wonders
  #{:dimensional-gate
    :planetdream
    :beacon-institute
    :ark-launchpad
    :planet-buster})

(s/def ::wonder wonders)
(s/def ::transformational-wonder transformational-wonders)