(ns dimdark.monsters 
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]
            [dimdark.abilities :as a]))

(s/def ::vulns ::a/traits)

(def cultures #{:orc :mechini :spider :undead :demon :hooman :goblin :slime :troll})
(s/def ::culture cultures)

(def monster-growth
  {:goblin {}
   :orc {}
   :spider {}
   :demon {}
   :undead {}
   :slime {}
   :troll {}
   :hooman {}
   :mechini {}})

(def monster-classes
  {:goblin
   {:raider
    {:abilities [:hew :net :trample :razor-pilum :plunderer]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :warg
    {:abilities [:bite :takedown :flank :rend :howl]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :mancer
    {:abilities [:poison-dart :knit-flesh :putrefy :flesh-offering :???]
     :growth {}
     :vulns #{:physical}
     :row :back}
    :junker
    {:abilities [:blunderblast :tinker-tailor :oil-bomb :war-machine :???]
     :growth {}
     :vulns #{:fire}
     :row :back}}
   :orc
   {:berserker
    {:abilities [:attack :blitz :battlecry :rampage :do-and-die]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :warhead
    {:abilities [:augment :organize :rally :browbeat :master-tactician]
     :growth {}
     :vulns #{:frost}
     :row :back}
    :bloodmucker
    {:abilities [:essence-bolt :searing-lash :devitalize :bloodlust :sacrifice]}}})

(def classes (set (flatten (map keys (vals monster-classes)))))
(s/def ::class classes)

(def abilities
  (set
   (flatten
    (for [class->details (vals monster-classes)]
      (for [{:keys [abilities]} (vals class->details)]
        abilities)))))
(s/def ::ability abilities)
(s/def ::abilities (s/coll-of ::ability))

(defn gen-monster
  ([level]
   (gen-monster level (rand-nth (keys monster-classes))))
  ([level culture]
   (gen-monster level culture (rand-nth (keys (culture monster-classes)))))
  ([level culture klass]
   (let [{:keys [growth vulns abilities row]} (klass (culture monster-classes))
         leveled-growth (map
                         (fn [[attr x]] [attr (* x level)])
                         (merge-with + (culture monster-growth) growth))
         attributes (into {} (filter #(contains? d/attributes (first %)) leveled-growth))
         merits (into {} (filter #(contains? d/merits (first %)) leveled-growth))]
     {:stats (d/attributes+merits->stats attributes merits)
      :abilities (set (subvec abilities 0 (inc level)))
      :effects {}
      :vulns vulns
      :row row
      :preferred-row row})))

(s/fdef gen-monster
  :args (s/cat :level ::d/level
               :culture (s/? ::culture)
               :class (s/? ::class))
  :ret ::d/creature)

(s/def ::monster
  (s/merge ::d/creature
           (s/keys :req-un [::vulns])))
