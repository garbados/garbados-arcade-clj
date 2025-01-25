(ns dimdark.combat 
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]))

(s/def ::party (s/coll-of ::d/creature :kind seq? :max-count 4))

(defn get-turn-order [kobolds monsters]
  (->> (into kobolds monsters)
       (filter
        (fn [{:keys [health]}]
          (pos-int? health)))
       (sort-by
        (fn [{:keys [stats effects]}]
          (:initiative (d/stats+effects->stats stats effects))))
       (map :id)))

(s/fdef get-turn-order
  :args (s/cat :kobolds ::party
               :monsters ::party)
  :ret (s/coll-of ::d/id))

(s/def ::rolls (s/coll-of (s/int-in 1 5)))

(defn roll
  "Roll NdM. Roll d6 by default."
  ([]
   (roll 1 6))
  ([n]
   (roll n 6))
  ([n m]
   (take n (repeatedly #(inc (rand-int m))))))

(s/fdef roll
  :args (s/cat :n pos-int?)
  :ret ::rolls)

(defn rolls+armor->damage [rolls armor]
  (max 0 (reduce (partial + (- armor)) 0 rolls)))

(s/fdef rolls+armor->damage
  :args (s/cat :rolls ::rolls
               :armor int?)
  :ret nat-int?)
