(ns dimdark.monsters 
  (:require [clojure.spec.alpha :as s]
            [dimdark.core :as d]))

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
    {:skills [:attack :net :trample :razor-pilum :plunderer]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :warg
    {:skills [:attack :takedown :flank :rend :howl]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :mancer
    {:skills [:poison-dart :knit-flesh :putrefy :flesh-offering :???]
     :growth {}
     :vulns #{:physical}
     :row :back}
    :junker
    {:skills [:blunderblast :tinker-tailor :oil-bomb :war-machine :???]
     :growth {}
     :vulns #{:fire}
     :row :back}}})

(defn gen-monster
  ([level]
   (gen-monster level (rand-nth (keys monster-classes))))
  ([level culture]
   (gen-monster level culture (rand-nth (keys (culture monster-classes)))))
  ([level culture klass]
   (let [{:keys [growth vulns skills row]} (klass (culture monster-classes))
         attributes (map
                     (fn [[attr x]] [attr (* x level)])
                     (merge-with + (culture monster-growth) growth))]
     {:stats (d/attributes->stats attributes)
      :skills (set (subvec skills 0 (inc level)))
      :effects {}
      :vulns vulns
      :row row})))