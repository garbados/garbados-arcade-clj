(ns planetcall.improvements
  (:require [clojure.spec.alpha :as s]))

(def primary-resources #{:food :material :energy})
(def all-resources (set (conj (seq primary-resources) :knowledge)))

(s/def ::primary primary-resources)
(s/def ::knowledge pos-int?)
(s/def ::impact int?)
(s/def ::modifier int?)
(s/def ::upkeep (s/map-of ::primary pos-int?))
(s/def ::cost ::upkeep)

(s/def ::improvement
  (s/keys :opt
          [::primary
           ::knowledge
           ::modifier
           ::impact
           ::cost
           ::upkeep]))

(def improvement->details
  {:farm        {:primary   :food}
   :workblock   {:primary   :material
                 :upkeep    {:food 1}}
   :reactor     {:primary   :energy
                 :upkeep    {:material 1}}
   :laboratory  {:knowledge 2
                 :upkeep    {:energy 1}}
   :stockpile   {}
   :road        {}
   :temple      {:primary   :food
                 :knowledge 1
                 :impact    -1}
   :museum      {:knowledge 4
                 :upkeep    {:energy 1}}
   :mine        {:primary   :material
                 :modifier  2
                 :impact    1
                 :upkeep    {:food 1}}
   :bunker      {:upkeep    {:food 1
                             :energy 1}}
   :rocket-silo {:upkeep    {:material 1
                             :energy   1}}
   :sensor      {:knowledge 1
                 :upkeep    {:energy 1}}
   :pulse-tower {:upkeep    {:energy 3}}
   :condenser   {:upkeep    {:energy 2}
                 :impact    3}
   :borehole    {:upkeep    {:energy 2}
                 :impact    3}
   :refractor   {:upkeep    {:energy 2}
                 :impact    3}})

(for [[_ improvement] (seq improvement->details)]
  (s/valid? ::improvement improvement))

(def improvements (set (keys improvement->details)))
