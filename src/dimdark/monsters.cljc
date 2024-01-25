(ns dimdark.monsters 
  (:require [clojure.edn :as edn]
            [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [clojure.string :as string]
            [dimdark.abilities :as a]
            [dimdark.core :as d]
            #?(:clj [arcade.text :refer [inline-slurp]]
               :cljs [arcade.text :refer-macros [inline-slurp]])))

(s/def ::vulns ::a/traits)

(def cultures #{:orc :mechini :spider :undead :demon :hooman :goblin :slime :troll})
(s/def ::culture cultures)

;; monster cultures get 11 points
(def monster-growth
  (edn/read-string (inline-slurp "resources/dimdark/monsters/growth.edn")))

(def monster-classes
  (edn/read-string (inline-slurp "resources/dimdark/monsters/classes.edn")))

(def classes (set (flatten (map keys (vals monster-classes)))))
(s/def ::class classes)

(def abilities (set (flatten (map :abilities (flatten (map vals (vals monster-classes)))))))
(s/def ::ability abilities)
(s/def ::abilities (s/coll-of ::ability :max-count 5))

(defn gen-monster
  ([level]
   (gen-monster level (rand-nth (keys monster-classes))))
  ([level culture]
   (gen-monster level culture (rand-nth (keys (culture monster-classes)))))
  ([level culture klass]
   (let [{:keys [growth vulns abilities row]} (klass (culture monster-classes))
         [attributes merits]
         (let [[attrs1 merits1] (d/parse-growth growth)
               [attrs2 merits2] (d/parse-growth (culture monster-growth))]
           [(merge-with + attrs1 attrs2)
            (merge-with + merits1 merits2)])
         {:keys [health] :as stats} (d/attributes+merits->stats attributes merits)]
     {:name (keyword (string/join "-" (map name [klass culture])))
      :stats (assoc stats :row row)
      :abilities (set (subvec abilities 0 (min (inc level) (count abilities))))
      :effects {}
      :vulns vulns
      :health health
      :row row})))

(s/fdef gen-monster
  :args (s/with-gen
          (s/cat :level ::d/level
                 :culture (s/? ::culture)
                 :class (s/? ::class))
          #(g/fmap
            (fn [[level culture]]
              (let [klass (rand-nth (keys (culture monster-classes)))]
                [level culture klass]))
            (g/tuple (s/gen ::d/level) (s/gen ::culture))))
  :ret ::d/creature)

(s/def ::monster
  (s/merge ::d/creature
           (s/keys :req-un [::vulns])))
