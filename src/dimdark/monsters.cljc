(ns dimdark.monsters 
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.gen.alpha :as g]
            [dimdark.abilities :as a]
            [dimdark.core :as d]
            [clojure.string :as string]))

(s/def ::vulns ::a/traits)

(def cultures #{:orc :mechini :spider :undead :demon :hooman :goblin :slime :troll})
(s/def ::culture cultures)

(def LOW 1)
(def MEDIUM 2)
(def HIGH 3)

;; monster cultures get 11 points
(def monster-growth
  {:goblin {:prowess LOW :alacrity MEDIUM :vigor LOW :spirit HIGH :focus MEDIUM :stink LOW :brat LOW}
   :orc {:prowess HIGH :alacrity MEDIUM :vigor MEDIUM :spirit LOW :focus LOW :stink LOW :squish LOW}
   :spider {:prowess LOW :alacrity HIGH :vigor MEDIUM :spirit LOW :focus MEDIUM :brat MEDIUM}
   :demon {:prowess MEDIUM :alacrity MEDIUM :vigor LOW :spirit LOW :focus HIGH :scales LOW :brat LOW}
   :undead {:prowess MEDIUM :alacrity LOW :vigor HIGH :spirit MEDIUM :focus LOW :stink MEDIUM}
   :slime {:prowess LOW :alacrity HIGH :vigor HIGH :spirit MEDIUM :focus MEDIUM}
   :troll {:prowess HIGH :alacrity LOW :vigor HIGH :spirit LOW :focus LOW :squish MEDIUM}
   :hooman {:prowess MEDIUM :alacrity MEDIUM :vigor MEDIUM :spirit MEDIUM :focus MEDIUM :brat LOW}
   :mechini {:prowess MEDIUM :alacrity LOW :vigor MEDIUM :spirit MEDIUM :focus HIGH :scales LOW}})

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
     :vulns #{:fire}
     :row :front}
    :warhead
    {:abilities [:augment :organize :rally :browbeat :master-tactician]
     :growth {}
     :vulns #{:poison}
     :row :back}
    :bloodmucker
    {:abilities [:essence-bolt :searing-lash :devitalize :bloodlust :sacrifice]
     :growth {}
     :vulns #{:frost}
     :row :back}
    :grunt
    {:abilities [:attack :bash :bully :soldier :first-aid]
     :growth {}
     :vulns #{:mental}
     :row :front}}
   :spider
   {:myrmidon
    {:abilities [:attack :mandible-crush :grapple :steelskin :eight-leg-assault]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :weaver
    {:abilities [:dream-eater :mesmerize :ennervate :sleep :???]
     :growth {}
     :vulns #{:fire}
     :row :back}
    :trapper
    {:abilities [:attack :string-shot :drag :lay-web :feed]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :widow
    {:abilities [:attack :poison-bite :acid-spray :concentrated-bile :virulent-climax]
     :growth {}
     :vulns #{:frost}
     :row :front}}
   :troll
   {:cave
    {:abilities [:attack :smash :regeneration :hurl-rock :rage]
     :growth {}
     :vulns #{:fire}
     :row :front}
    :ancient
    {:abilities [:attack :regeneration :crush :venerable :revitalize]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :forest
    {:abilities [:vine-lash :spore-burst :regeneration :root-swarm :moss-grave]
     :growth {}
     :vulns #{:fire}
     :row :back}
    :grizzly
    {:abilities [:attack :ravage :regeneration :devour :roar]
     :growth {}
     :vulns #{:mental}
     :row :front}}
   :undead
   {:banshee
    {:abilities [:haunt :scream :chilling-touch :reverie :vanish]
     :growth {}
     :vulns #{:fire}
     :row :back}
    :awoken
    {:abilities [:attack :fetid-bite :jagged-nail :consume-corpse :dire-ghoul]
     :growth {}
     :vulns #{:fire}
     :row :front}
    :shadow
    {:abilities [:attack :paralyze :umbra-slash :darkness-falls :vantablack]
     :growth {}
     :vulns #{:fire}
     :row :front}
    :necromancer
    {:abilities [:spirit-link :horrify :drink-terror :necrotize :sacrifice]
     :growth {}
     :vulns #{:physical}
     :row :back}}
   :demon
   {:succubus
    {:abilities [:pain-lash :tempt :seduce :consume-essence :???]
     :growth {}
     :vulns #{:frost}
     :row :back}
    :fiend
    {:abilities [:attack :bind :torture :menace :internal-wretch]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :vengeful
    {:abilities [:attack :brutal-envy :mad-rage :doom :so-below]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :ornias
    {:abilities [:absorb-agony :exhale-suffering :languish :haunting-cry :parasite]
     :growth {}
     :vulns #{:fire}
     :row :back}}
   :slime
   {:natto
    {:abilities [:attack :venom-splash :noxious-gas :gelatinous-prism :intoxicating-ooze]
     :growth {}
     :vulns #{:mental}
     :row :front}
    :flan
    {:abilities [:fire-spit :magma-slime :searing-goo :furnace-belly :???]
     :growth {}
     :vulns #{:frost}
     :row :back}
    :sorbet
    {:abilities [:icicles :melt :igloo :frost-nova :???]
     :growth {}
     :vulns #{:fire}
     :row :back}
    :muckling
    {:abilities [:attack :mud-fight :grit-slap :glomp :goop-blast]
     :growth {}
     :vulns #{:poison}
     :row :front}}
   :mechini
   {:needlecrab
    {:abilities [:attack :puncture :razor-slice :claw-crush :king-crab]
     :growth {}
     :vulns #{:frost}
     :row :front}
    :steelfly
    {:abilities [:fire-nail :??? :??? :??? :???]
     :growth {}
     :vulns #{:physical}
     :row :back}
    :manufacterist
    {:abilities [:repair :overdrive :upgrade :construct-ally :industrialist]
     :growth {}
     :vulns #{:fire}
     :row :back}
    :rockbiter
    {:abilities [:attack :drill-strike :stone-drop :collapse-roof :grit-golem]
     :growth {}
     :vulns #{:mental}
     :row :front}}
   :hooman
   {:paladin
    {:abilities [:attack :judgment :holy-shield :penitent-blow :lay-on-hands]
     :growth {}
     :vulns #{:poison}
     :row :front}
    :cleric
    {:abilities [:cure :panacea :purify :healing-nova :wizened-healer]
     :growth {}
     :vulns #{:mental}
     :row :back}
    :wizard
    {:abilities [:frostbolt :fireball :force-shield :center-the-eye :master-arcanist]
     :growth {}
     :vulns #{:poison}
     :row :back}
    :bard
    {:abilities [:rousing-anthem :intimidating-chant :grave-dirge :shocking-aria :maestro]
     :growth {}
     :vulns #{:mental}
     :row :back}
    :gladiator
    {:abilities [:attack :assault :leap-slam :whirlwind :shrug-off]
     :growth {}
     :vulns #{:mental}
     :row :front}}})

(def classes (set (flatten (map keys (vals monster-classes)))))
(s/def ::class classes)

(def abilities
  (set
   (flatten
    (for [class->details (vals monster-classes)]
      (for [{:keys [abilities]} (vals class->details)]
        abilities)))))
(s/def ::ability abilities)
(s/def ::abilities (s/coll-of ::ability :max-count 5))

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
     {:name (keyword (string/join "-" (map (comp string/capitalize name) [klass culture])))
      :stats (assoc (d/attributes+merits->stats attributes merits) :row row)
      :abilities (set (subvec abilities 0 (min (inc level) (count abilities))))
      :effects {}
      :vulns vulns
      :row row
      :preferred-row row})))

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
