(ns planetcall.factions
  (:require [clojure.spec.alpha :as s]
            [planetcall.geometry :as geo]
            [planetcall.ideotech :as pi]
            [planetcall.improvements :refer [primary-resources]]
            [planetcall.units :as pu]
            [planetcall.wonders :as pw]
            [planetcall.spaces :as ps]))

(s/def ::designs (s/coll-of ::pu/design))
(s/def ::claimed (s/coll-of ::geo/coord :kind set?))
(s/def ::wonders (s/map-of ::pw/wonder (s/int-in 1 21)))
(s/def ::stockpile
  (s/map-of primary-resources
            (s/int-in 0 21)
            :min-count (count primary-resources)
            :max-count (count primary-resources)
            :distinct true))
(s/def ::stockpiles (s/map-of ::geo/coord ::stockpile))
(s/def ::researched ::pi/ideotechs)
(s/def ::current-research (s/cat :research (s/nilable ::pi/ideotech)
                                 :progress nat-int?))
(s/def ::bonus-research nat-int?)
(s/def ::researching
  (s/map-of ::pi/ideotech pos-int?))
(s/def ::experience (s/map-of ::pi/ideology nat-int?))

(def conditions #{:plenitude :scarcity})
(s/def ::condition conditions)
(s/def ::conditions (s/map-of ::condition any?))

(s/def ::coord->space (s/map-of ::geo/coord ::ps/space))
(s/def ::last-seen (s/keys :req-un [::coord->space
                                    ::coord->units]))

(s/def ::faction (s/keys :req-un
                         [::designs
                          ::claimed
                          ::wonders
                          ::stockpiles
                          ::researched
                          ::current-research
                          ::bonus-research
                          ::researching
                          ::experience
                          ::conditions
                          ::last-seen]))
