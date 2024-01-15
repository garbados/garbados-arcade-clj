(ns planetcall.spaces
  (:require [clojure.spec.alpha :as s]
            [planetcall.improvements :refer [improvements
                                             improvement->details
                                             primary-resources
                                             all-resources]]
            [planetcall.wonders :refer [wonders]]))

(def rainfalls [:arid :moist :rainy :lush])
(def terrains [:flat :rolling :hilly :mountaineous])
(def vegetations #{:fungus :forest})
(def features #{:river :xenobog :ruin :rare-metals :thermal-vents})

(def resource->attribute
  {:food     :rainfall
   :material :terrain
   :energy   :elevation})

(def attribute->levels
  {:rainfall rainfalls
   :terrain  terrains})

(def feature->resource
  {:xenobog       :food
   :rare-metals   :material
   :thermal-vents :energy
   :ruin          :knowledge})

(def all-improvements (into improvements wonders))

(s/def ::elevation (s/int-in -4 7))
(s/def ::rainfall (set rainfalls))
(s/def ::terrain (set terrains))
(s/def ::vegetation (s/nilable vegetations))
(s/def ::feature (s/nilable features))
(s/def ::improvement (s/nilable all-improvements))
(s/def ::radiation (s/int-in 0 4))
(s/def ::miasma boolean?)
(s/def ::space
  (s/keys :req-un
          [::elevation
           ::rainfall
           ::terrain
           ::vegetation
           ::feature
           ::improvement
           ::radiation
           ::miasma]))

(defn gen-unclaimed-space []
  {:elevation (- (rand-int 11) 4)
   :rainfall (rand-nth rainfalls)
   :terrain (rand-nth terrains)
   :vegetation (rand-nth [nil :fungus])
   :feature (rand-nth (concat
                       ;; 50% chance of nothing
                       (map (constantly nil) (range (count features)))
                       ;;chance of something
                       features))
   :improvement nil
   :radiation (rand-int 3)
   :miasma (rand-nth [true false])})

(s/def ::resource-map (s/map-of all-resources int?))
(s/def ::yield ::resource-map)
(s/def ::upkeep ::resource-map)

(defn resource->modifier
  [space resource]
  {:pre [(s/valid? ::space space) (primary-resources resource)]
   :post [int?]}
  (let [attribute (resource->attribute resource)
        value (space attribute)]
    (if (= attribute :elevation)
      (max 0 value)
      ;; get index of rainfall or terrain value
      (.indexOf (attribute->levels attribute) value))))

(defn get-space-yield
  "Return the yield of a space."
  [space]
  {:pre [(s/valid? ::space space)]
   :post [#(s/valid? ::yield %)]}
  (if-let [improvement (-> space :improvement)]
    (let [details  (get improvement->details improvement {})
          yield    {:knowledge (or (details :knowledge) 0)}
          resource (details :primary)
          feature-bonus
          (if (= resource
                 (-> space :feature feature->resource))
            2
            0)]
      (if resource
        (assoc yield resource
               (+ (or (details :modifier) 0)
                  (resource->modifier space resource)
                  feature-bonus))
        yield))
    {}))

(defn get-space-upkeep
  "Return the upkeep of a space."
  [space]
  {:pre [(s/valid? ::space space)]
   :post [#(s/valid? ::upkeep %)]}
  (if-let [improvement (space :improvement)]
    (or (-> improvement improvement->details :upkeep)
        {})
    {}))
