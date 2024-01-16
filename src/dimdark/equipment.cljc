(ns dimdark.equipment 
  (:require [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::level (s/int-in 1 6))

(s/def :weapon/type
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

(s/def :armor/type
  #{:padded
    :leather
    :hide
    :chain
    :splint
    :plate})

(s/def :armor/class
  #{:light :medium :heavy})

(def armor->class
  {:padded :light
   :leather :light
   :hide :medium
   :chain :medium
   :splint :heavy
   :plate :heavy})

(s/def :accessory/type
  #{:ring
    :amulet
    :talisman
    :charm})

(s/def ::type
  (s/or :weapon :weapon/type
        :armor :armor/type
        :accessory :accessory/type))

(def slots #{:weapon :armor :accessory})
(s/def ::slot slots)

(def stat->modifiers
  {:health [:durable :hale :stalwart :fecund :vigorous]
   :attack [:polished :honed :tempered :gleaming :flaring]
   :defense [:lacquered :studded :reinforced :girded :impregnable]
   :initiative [:nimble :swift :fleet :spry :sprightly]
   :fortune [:lucky :favored :blessed :auspicious :prosperous]
   :aptitude [:skilled :adept :proficient :adroit :accomplished]
   :fire-aptitude [:singed :burned :smoldering :seared :scalded]
   :frost-aptitude [:chilly :cold :frigid :frozen :glacial]
   :poison-aptitude [:septic :pernicious :noxious :baleful :virulent]
   :mental-aptitude [:charming :persuasive :eloquent :alluring :seductive]
   :resistance [:shimmering :reflective :crystalline :prismatic :mosaic]
   :fire-resistance [:intumescent :perlite :aramid :melamine :modacrylic]
   :frost-resistance [:warm :insulated :thermal :wintry :arctic]
   :poison-resistance [:clean :antiseptic :sterile :medical :surgical]
   :mental-resistance [:firm :insistent :decisive :irrepressible :indomitable]
   :scales [:piscine :serpentine :ophidian :saurian :draconic]
   :squish [:soft :plush :stout :plump :rotund]
   :stink [:musty :malodorous :foul :fetid :putrid]
   :brat [:cheeky :bold :brazen :impudent :insolent]})

(def modifiers (set (reduce concat (vals stat->modifiers))))
(s/def ::modifiers (s/coll-of modifiers :max-count 3))

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

(s/def ::equipment
  (s/keys :req-un [::name
                   ::type
                   ::level
                   ::slot
                   ::modifiers]))


(def rare-first-word
  ["Agony",
   "Arachnid",
   "Armageddon",
   "Bear",
   "Beast",
   "Bitter",
   "Blackhorn",
   "Blood",
   "Bone",
   "Bramble",
   "Brimstone",
   "Carrion",
   "Chaos",
   "Corpse",
   "Corruption",
   "Crow",
   "Cruel",
   "Dark",
   "Dim",
   "Dire",
   "Death",
   "Demon",
   "Doom",
   "Dread",
   "Eagle",
   "Entropy",
   "Feather",
   "Fiend",
   "Gale",
   "Ghoul",
   "Glyph",
   "Grim",
   "Hate",
   "Havoc",
   "Imp",
   "Infernal",
   "Limestone",
   "Loath",
   "Order",
   "Pain",
   "Plague",
   "Raven",
   "Rule",
   "Rune",
   "Shadow",
   "Skull",
   "Stone",
   "Storm",
   "Sol",
   "Soul",
   "Spirit",
   "Terror",
   "Tyranny",
   "Viper",
   "Warg",
   "Wraith",
   "Wretched"])

(def rare-second-word
  {"Aegis" [:chain :splint :plate]
   "Badge" [:padded :leather :hide]
   "Band" [:ring]
   "Bane" [:axe :club :dagger :staff]
   "Bar" [:club :spear :polearm]
   "Barb" [:sword :dagger :claws :greataxe :polearm]
   "Bastion" [:splint :plate]
   "Bauble" [:ring :charm]
   "Beads" [:ring :charm]
   "Beak" [:dagger :sword]
   "Bite" [:claws :dagger :sword :polearm]
   "Blazer" [:hide :chain]
   "Blow" [:club]
   "Bludgeon" [:club :staff]
	;; "Bolt": [CROSSBOW],
	;; "Book": [TOME],
	;; "Branch": [BOW, STAFF, SPEAR, POLEARM],
	;; "Brand": [GREATAXE, GREATMACE, STAFF],
	;; "Breaker": [MACE, GREATMACE],
	;; "Carapace": [MEDIUM_ARMOR, HEAVY_ARMOR],
	;; "Cataphract": [HEAVY_ARMOR],
	;; "Chronicle": [TOME],
	;; "Circle": [RING, CHARM],
	;; "Clasp": [BANGLE],
	;; "Claw": [CLAWS],
	;; "Cleaver": [AXE, GREATSWORD],
	;; "Cloak": [LIGHT_ARMOR],
	;; "Clutches": [BRACER],
	;; "Coat": [LIGHT_ARMOR, MEDIUM_ARMOR],
	;; "Coil": [RING, CHARM, BANGLE],
	;; "Collar": [CHARM],
	;; "Companion": [TOME, CHARM],
	;; "Cord": [BANGLE, CHARM],
	;; "Cowl": [LIGHT_ARMOR],
	;; "Crack": [MACE],
	;; "Crest": [LIGHT_ARMOR],
	;; "Crusher": [MACE, GREATMACE],
	;; "Cry": [DAGGER, CLAWS],
	;; "Cuirass": [HEAVY_ARMOR],
	;; "Dart": [CROSSBOW, SPEAR],
	;; "Edge": [DAGGER, SWORD],
	;; "Emblem": [RING, CHARM, BANGLE, BRACER],
	;; "Eye": [RING, CHARM],
	;; "Fang": [SWORD, GREATSWORD, GREATAXE],
	;; "Flange": [MACE],
	;; "Fletch": [BOW, CROSSBOW],
	;; "Flight": [BOW, CROSSBOW],
	;; "Finger": [BRACER, RING],
	;; "Fringe": [LIGHT_ARMOR, BANGLE],
	;; "Gnarl": [MACE],
	;; "Gnash": [AXE, MACE],
	;; "Goad": [SPEAR, STAFF],
	;; "Gorget": [CHARM],
	;; "Grasp": [BRACER, RING],
	;; "Grinder": [MACE, GREATMACE],
	;; "Grip": [BRACER, RING],
	;; "Guard": [BRACER],
	;; "Gutter": [DAGGER, SWORD, GREATSWORD],
	;; "Gyre": [RING],
	;; "Hand": [BRACER],
	;; "Harness": [MEDIUM_ARMOR],
	;; "Harp": [BOW, CROSSBOW],
	;; "Hide": [MEDIUM_ARMOR],
	;; "Heart": [CHARM],
	;; "Hew": [SWORD, AXE, GREATSWORD, GREATAXE],
	;; "Hold": [BRACER, RING],
	;; "Horn": [DAGGER, BOW, CROSSBOW],
	;; "Impaler": [DAGGER, SWORD, SPEAR],
	;; "Jack": [LIGHT_ARMOR],
	;; "Knell": [MACE],
	;; "Knot": [RING],
	;; "Knuckle": [RING],
	;; "Lance": [SPEAR, POLEARM],
	;; "Lash": [MACE],
	;; "Ledger": [TOME],
	;; "Lock": [CHARM],
	;; "Log": [TOME, STAFF, MACE, GREATMACE],
	;; "Loom": [CROSSBOW],
	;; "Loop": [RING],
	;; "Mallet": [MACE, GREATMACE],
	;; "Mangler": [AXE, MACE],
	;; "Mantle": [LIGHT_ARMOR, MEDIUM_ARMOR, HEAVY_ARMOR],
	;; "Manual": [TOME],
	;; "Mar": [SWORD, AXE, GREATSWORD, SPEAR],
	;; "Mark": [RING, CHARM],
	;; "Mask": [CHARM],
	;; "Mast": [STAFF],
	;; "Mistress": [RING, CHARM],
	;; "Nail": [SPEAR],
	;; "Needle": [BOW, DAGGER, SPEAR],
	;; "Nock": [BOW],
	;; "Noose": [CHARM],
	;; "Opus": [TOME],
	;; "Pale": [STAFF, POLEARM],
	;; "Pelt": [LIGHT_ARMOR, MEDIUM_ARMOR],
	;; "Picket": [AXE, SPEAR],
	;; "Pillar": [STAFF],
	;; "Prod": [SPEAR],
	;; "Quarrel": [BOW, CROSSBOW],
	;; "Quill": [DAGGER, SWORD, BOW, CROSSBOW, SPEAR],
	;; "Razor": [CLAWS, DAGGER, SWORD, AXE, GREATSWORD, GREATAXE],
	;; "Reaver": [AXE, GREATAXE],
	;; "Rend": [AXE, GREATAXE],
	;; "Rock": [RING, CHARM],
	;; "Saw": [GREATSWORD],
	;; "Scalpel": [DAGGER, SWORD],
	;; "Scourge": [SPEAR],
	;; "Scratch": [DAGGER, SWORD, CLAWS, POLEARM],
	;; "Scythe": [AXE, GREATAXE, POLEARM],
	;; "Sever": [DAGGER, SWORD],
	;; "Shank": [DAGGER],
	;; "Shell": [CHARM, HEAVY_ARMOR],
	;; "Shroud": [LIGHT_ARMOR],
	;; "Skewer": [DAGGER, SWORD, SPEAR],
	;; "Slayer": [AXE],
	;; "Smasher": [MACE, GREATMACE],
	;; "Song": [DAGGER, SWORD, AXE, GREATSWORD, BOW, CLAWS, POLEARM, CHARM, TOME],
	;; "Spawn": [AXE, CHARM],
	;; "Spike": [DAGGER, SWORD, GREATSWORD],
	;; "Spiral": [RING, CHARM],
	;; "Splitter": [AXE, GREATAXE],
	;; "Stake": [SWORD, GREATSWORD, STAFF, SPEAR, POLEARM],
	;; "Stalker": [LIGHT_ARMOR],
	;; "Standard": [STAFF, POLEARM],
	;; "Star": [RING, CHARM, MACE, GREATMACE],
	;; "Stinger": [DAGGER, SWORD, GREATSWORD, BOW, CROSSBOW],
	;; "Strap": [BANGLE],
	;; "Suit": [MEDIUM_ARMOR, HEAVY_ARMOR],
	;; "Sunder": [AXE, GREATAXE],
	;; "Talisman": [CHARM],
	;; "Talon": [DAGGER, SWORD, GREATSWORD],
	;; "Thirst": [AXE, CLAWS, SWORD, BOW, SPEAR, POLEARM],
	;; "Tooth": [DAGGER, SWORD],
	;; "Torc": [CHARM],
	;; "Torch": [MACE],
	;; "Touch": [BRACER, RING],
	;; "Turn": [RING],
	;; "Treatise": [TOME],
	;; "Veil": [LIGHT_ARMOR],
	;; "Volume": [TOME],
	;; "Wand": [MACE],
	;; "Ward": [CHARM],
	;; "Weaver": [STAFF],
	;; "Wing": [CHARM],
	;; "Whorl": [RING, CHARM],
	;; "Wood": [MACE, SPEAR, STAFF],
	;; "Word": [STAFF, TOME],
	;; "Wrack": [AXE, SPEAR, POLEARM],
	;; "Wrap": [LIGHT_ARMOR, MEDIUM_ARMOR]
   })