(ns dimdark.combat 
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]))

(s/def ::party (s/coll-of ::d/creature :kind seq? :max-count 4))

(defn get-turn-order [kobolds monsters]
  (sort-by
   (fn [{:keys [stats effects]}]
     (:initiative (d/stats+effects->stats stats effects)))
   (into kobolds monsters)))

(s/fdef get-turn-order
  :args (s/cat :kobolds ::party
               :monsters ::party)
  :ret (s/coll-of ::d/creature))

(s/def ::rolls (s/coll-of (s/int-in 1 5)))

(defn roll-damage
  "Roll 1d4 x magnitude"
  [n]
  (take n (repeatedly #(inc (rand-int 4)))))

(s/fdef roll-damage
  :args (s/cat :n pos-int?)
  :ret ::rolls)

(defn rolls+armor=>damage [rolls armor]
  (reduce + 0 (map #(max 0 (- % armor)) rolls)))

(s/fdef rolls+armor=>damage
  :args (s/cat :rolls ::rolls
               :armor (s/int-in 0 4))
  :ret nat-int?)
