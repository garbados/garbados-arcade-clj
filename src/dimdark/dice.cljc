(ns dimdark.dice 
  (:require
   [clojure.spec.alpha :as s]))

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