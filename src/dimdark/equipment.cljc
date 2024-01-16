(ns dimdark.equipment 
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [dimdark.core :as d]))

(s/def ::name string?)
(s/def ::level (s/int-in 1 6))

(def weapons
  #{:sword
    :axe
    :spear
    :polearm
    :bow
    :crossbow
    :dagger
    :claws
    :tome
    :orb
    :staff
    :club})
(s/def :weapon/type weapons)

(def armors
  #{:padded
    :leather
    :hide
    :chain
    :splint
    :plate})
(s/def :armor/type armors)

(s/def :armor/class
  #{:light :medium :heavy})

(def armor->class
  {:padded :light
   :leather :light
   :hide :medium
   :chain :medium
   :splint :heavy
   :plate :heavy})

(def accessories
  #{:ring
    :amulet
    :charm
    :bangle})
(s/def :accessory/type accessories)

(s/def ::type
  (s/or :weapon :weapon/type
        :armor :armor/type
        :accessory :accessory/type))

(def type->slot
  (merge
   (reduce
    (fn [type->slot weapon]
      (assoc type->slot weapon :weapon))
    {}
    weapons)
   (reduce
    (fn [type->slot armor]
      (assoc type->slot armor :armor))
    {}
    armors)
   (reduce
    (fn [type->slot accessory]
      (assoc type->slot accessory :accessory))
    {}
    accessories)))

(def slots #{:weapon :armor :accessory})
(s/def ::slot slots)

(def stat->modifiers
  {:health [:durable :hale :stalwart :fecund :vigorous]
   :attack [:polished :honed :tempered :gleaming :fla:ring]
   :defense [:lacquered :studded :reinforced :girded :impregnable]
   :initiative [:nimble :swift :fleet :spry :sprightly]
   :fortune [:lucky :favored :blessed :auspicious :prosperous]
   :aptitude [:skilled :adept :proficient :adroit :accomplished]
   :fire-aptitude [:singed :burned :smolde:ring :seared :scalded]
   :frost-aptitude [:chilly :cold :frigid :frozen :glacial]
   :poison-aptitude [:septic :pernicious :noxious :baleful :virulent]
   :mental-aptitude [:charming :persuasive :eloquent :allu:ring :seductive]
   :resistance [:shimme:ring :reflective :crystalline :prismatic :mosaic]
   :fire-resistance [:intumescent :perlite :aramid :melamine :modacrylic]
   :frost-resistance [:warm :insulated :thermal :wintry :arctic]
   :poison-resistance [:clean :antiseptic :sterile :medical :surgical]
   :mental-resistance [:firm :insistent :decisive :irrepressible :indomitable]
   :scales [:piscine :serpentine :ophidian :saurian :draconic]
   :squish [:soft :plush :stout :plump :rotund]
   :stink [:musty :malodorous :foul :fetid :putrid]
   :brat [:cheeky :bold :brazen :impudent :insolent]})

(def modifiers (set (reduce concat (vals stat->modifiers))))
(s/def ::modifier modifiers)
(s/def ::modifiers (s/coll-of ::modifier :max-count 3))

(def modifier->details
  (reduce
   (fn [details [stat modifiers]]
     (reduce
      (fn [details i]
        (let [modifier (get modifiers i)
              value (inc i)]
          (assoc details modifier [stat value])))
      details
      (range (count modifiers))))
   {}
   (seq stat->modifiers)))

(defn rand-modifier
  ([level]
   (rand-nth (flatten (map (partial take level) (vals stat->modifiers)))))
  ([level valid-stats]
   (rand-nth (flatten (map (comp (partial take level) second) (filter (fn [[stat _]] (contains? valid-stats stat)) (seq stat->modifiers)))))))

(s/fdef rand-modifier
  :args (s/cat :level ::level)
  :ret ::modifier)

(s/def ::equipment
  (s/keys :req-un [::name
                   ::type
                   ::level
                   ::slot
                   ::modifiers]))

(defn gen-basic-equipment [type]
  {:name (cond-> (string/capitalize (name type))
           (contains? armors type) (str " Armor"))
   :type type
   :level 1
   :slot (type type->slot)
   :modifiers []})

(s/fdef gen-basic-equipment
  :args (s/cat :type ::type)
  :ret ::equipment)

(def weapon-type->stats
  {:axe [:attack]
   :sword [:attack :initiative]
   :spear [:attack]
   :polearm [:attack :aptitude]
   :bow [:attack]
   :crossbow [:attack :initiative]
   :dagger [:initiative]
   :claws [:attack :initiative]
   :tome [:aptitude :resistance]
   :orb [:aptitude]
   :staff [:defense]
   :club [:attack :defense]})

(defn weapon-level->stats [type level]
  (let [stats (type weapon-type->stats)]
    (if (= 1 (count stats))
      {(first stats) (* 2 level)}
      (reduce
       #(assoc %1 %2 level)
       {}
       stats))))

(s/fdef weapon-level->stats
  :args (s/cat :type :weapon/type
               :level ::level)
  :ret (s/map-of d/stats pos-int?))

(def armor-type->stats
  {:padded  {:armor 0
             :initiative 1}
   :leather {:armor 1}
   :hide    {:armor 1
             :initiative 1}
   :chain   {:armor 2}
   :splint  {:armor 2
             :initiative 1}
   :plate   {:armor 3}})

(defn armor-level->stats [type level]
  (let [stats (type armor-type->stats)]
    (if (= 1 (:initiative stats 0))
      (assoc stats :initiative level)
      stats)))

(s/fdef armor-level->stats
  :args (s/cat :type :armor/type
               :level ::level)
  :ret (s/map-of #{:armor :initiative} nat-int?))

(def rare-first-word
  ["Agony"
   "Arachnid"
   "Armageddon"
   "Bear"
   "Beast"
   "Bitter"
   "Blackhorn"
   "Blood"
   "Bone"
   "Bramble"
   "Brimstone"
   "Carrion"
   "Chaos"
   "Corpse"
   "Corruption"
   "Crow"
   "Cruel"
   "Dark"
   "Dim"
   "Dire"
   "Death"
   "Demon"
   "Doom"
   "Dread"
   "Eagle"
   "Entropy"
   "Feather"
   "Fiend"
   "Gale"
   "Ghoul"
   "Glyph"
   "Grim"
   "Hate"
   "Havoc"
   "Imp"
   "Infernal"
   "Limestone"
   "Loath"
   "Order"
   "Pain"
   "Plague"
   "Raven"
   "Rule"
   "Rune"
   "Shadow"
   "Skull"
   "Stone"
   "Storm"
   "Sol"
   "Soul"
   "Spirit"
   "Terror"
   "Tyranny"
   "Viper"
   "Warg"
   "Wraith"
   "Wretched"])

(def rare-second-word
  {"Aegis" [:chain :splint :plate]
   "Badge" [:padded :leather :hide]
   "Band" [:ring]
   "Bane" [:axe :club :dagger :staff]
   "Bar" [:club :spear :polearm]
   "Barb" [:sword :dagger :claws :polearm :polearm]
   "Bastion" [:splint :plate]
   "Bauble" [:ring :charm]
   "Beads" [:ring :charm]
   "Beak" [:dagger :sword]
   "Bite" [:claws :dagger :sword :polearm]
   "Blazer" [:hide :chain]
   "Blow" [:club]
   "Bludgeon" [:club :staff]
   "Bolt" [:crossbow]
   "Book" [:tome]
   "Branch" [:bow :staff :club :spear :polearm]
   "Brand" [:axe :club :staff :spear :polearm]
   "Breaker" [:club :staff :axe :polearm]
   "Carapace" [:hide :chain :splint :plate]
   "Cataphract" [:splint :plate]
   "Chronicle" [:tome]
   "Circle" [:ring :charm]
   "Clasp" [:amulet]
   "Claw" [:claws]
   "Cleaver" [:axe]
   "Cloak" [:padded :leather]
   "Clutches" [:bangle]
   "Coat" [:padded :leather :hide :chain]
   "Coil" [:ring :charm :amulet]
   "Collar" [:charm]
   "Companion" [:tome :charm]
   "Cord" [:amulet :charm]
   "Cowl" [:padded :leather]
   "Crack" [:club]
   "Crest" [:padded :leather]
   "Crusher" [:club :polearm]
   "Cry" [:dagger :claws]
   "Cuirass" [:splint :plate]
   "Dart" [:crossbow :spear]
   "Edge" [:dagger :sword]
   "Emblem" [:ring :charm :amulet :bangle]
   "Eye" [:ring :charm]
   "Fang" [:sword :polearm]
   "Flange" [:club]
   "Fletch" [:bow :crossbow]
   "Flight" [:bow :crossbow]
   "Finger" [:bangle :ring]
   "F:ringe" [:padded :leather :amulet]
   "Gnarl" [:club]
   "Gnash" [:axe :club]
   "Goad" [:spear :staff]
   "Gorget" [:charm]
   "Grasp" [:bangle :ring]
   "Grinder" [:club :polearm]
   "Grip" [:bangle :ring]
   "Guard" [:bangle]
   "Gutter" [:dagger :sword]
   "Gyre" [:ring]
   "Hand" [:bangle]
   "Harness" [:hide :chain]
   "Harp" [:bow :crossbow]
   "Hide" [:hide :chain]
   "Heart" [:charm]
   "Hew" [:sword :axe  :polearm]
   "Hold" [:bangle :ring]
   "Horn" [:dagger :bow :crossbow]
   "Impaler" [:dagger :sword :spear]
   "Jack" [:padded :leather]
   "Knell" [:club]
   "Knot" [:ring :orb]
   "Knuckle" [:ring]
   "Lance" [:spear :polearm]
   "Lash" [:club]
   "Ledger" [:tome]
   "Lock" [:charm]
   "Log" [:tome :staff :club :polearm]
   "Loom" [:crossbow]
   "Loop" [:ring :orb]
   "Mallet" [:club :polearm]
   "Mangler" [:axe :club]
   "Mantle" [:padded :leather :hide :chain :splint :plate]
   "Manual" [:tome]
   "Mar" [:sword :axe  :spear]
   "Mark" [:ring :charm]
   "Mask" [:charm]
   "Mast" [:staff]
   "Mistress" [:ring :charm]
   "Nail" [:spear]
   "Needle" [:bow :dagger :spear]
   "Nock" [:bow]
   "Noose" [:charm]
   "Opus" [:tome :orb]
   "Pale" [:staff :polearm]
   "Pelt" [:padded :leather :hide]
   "Picket" [:axe :spear]
   "Pillar" [:staff :polearm :spear]
   "Prod" [:spear :polearm]
   "Quarrel" [:bow :crossbow]
   "Quill" [:dagger :sword :bow :crossbow :spear]
   "Razor" [:claws :dagger :sword :axe  :polearm]
   "Reaver" [:axe :polearm]
   "Rend" [:axe :polearm]
   "Rock" [:ring :charm]
   "Saw" [:axe]
   "Scalpel" [:dagger :sword]
   "Scourge" [:spear]
   "Scratch" [:dagger :sword :claws :polearm]
   "Scythe" [:axe :polearm]
   "Sever" [:dagger :sword]
   "Shank" [:dagger]
   "Shell" [:charm :splint :plate]
   "Shroud" [:padded :leather]
   "Skewer" [:dagger :sword :spear]
   "Slayer" [:axe]
   "Smasher" [:club :polearm]
   "Song" [:dagger :sword :axe :bow :claws :polearm :charm :tome]
   "Spawn" [:axe :charm]
   "Spike" [:dagger :sword]
   "Spiral" [:ring :charm]
   "Splitter" [:axe :polearm]
   "Stake" [:sword  :staff :spear :polearm]
   "Stalker" [:padded :leather]
   "Standard" [:staff :polearm :spear]
   "Star" [:ring :charm :club :polearm :orb]
   "Stinger" [:dagger :sword :bow :crossbow]
   "Strap" [:amulet :bangle]
   "Suit" [:hide :chain :splint :plate]
   "Sunder" [:axe :polearm]
   "Talisman" [:charm]
   "Talon" [:dagger :sword :claws]
   "Thirst" [:axe :claws :sword :bow :spear :polearm]
   "Tooth" [:dagger :sword]
   "Torc" [:charm]
   "Torch" [:club]
   "Touch" [:bangle :ring]
   "Turn" [:ring]
   "Treatise" [:tome]
   "Veil" [:padded :leather]
   "Volume" [:tome]
   "Wand" [:club]
   "Ward" [:charm]
   "Weaver" [:staff]
   "Wing" [:charm]
   "Whorl" [:ring :charm :orb]
   "Wood" [:club :spear :staff]
   "Word" [:staff :tome :orb]
   "Wrack" [:axe :spear :polearm]
   "Wrap" [:padded :leather :hide]})

(def type->second-words
  (reduce
   (fn [type->words [word types]]
     (reduce
      (fn [type->words type]
        (if (contains? type->words type)
          (update type->words type conj word)
          (assoc type->words type [word])))
      type->words
      types))
   {}
   (seq rare-second-word)))

(defn name-rare [type]
  (str (rand-nth rare-first-word)
       " "
       (rand-nth (type type->second-words))))

(s/fdef name-rare
  :args (s/cat :type ::type)
  :ret string?)
