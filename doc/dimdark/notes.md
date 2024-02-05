# Notes

Five adventures, each with 1-2 monster types and a boss

- 1: The Despot's Crown: Orcs, Goblins; Orc Warlord
- 2: The Dragon-Tamer: Spiders, Demons; Spider Queen
- 3: Whom the Earth Adores: Slimes, Trolls; Troll King
- 4: Light and Hate: Hoomans, Undead; Master Inquisitor
- 5: Til Death: Mechini; Beryl Agatis the Haunted

Class and skill descriptions often have cool text! Check them while implementing!

Each scene grants one experience. After 20 experience, the party levels up; a year has passed. By 100 experience, the party will be at max level. *At 150 experience the dragon dies.* If the player completes Til Death before then, the kobolds manage to extract the crystal safely, saving the dragon and granting a different ending.

Til Death is unlocked with a journal found at Delving level 30.

*New Game Plus* begins at +20 experience, granting a free level but reducing the number of playable days and depriving the party of the loot it would have obtained in that span. There are thus only a few valid levels for New Game Plus, as at least 80 points are required to complete all the adventures and to find the map for Til Death: 0=>0, 1=>20,130; 2=>40,110; 3=>60,90. These correspond to Normal, Advanced, Expert.

New equipment paradigm:
- Each dragon has two preferred equipment types, with different implicits.
- Implicits scale with item level. Each level of each type has a different name. (Spear => Pike => Naginata, etc)

What's the deal with attributes, merits, and stats?
- Attributes and merits can be included in growth patterns.
- Merits and stats can be affected by gear.
- Stats can be affected by effects.

What about a new monster paradigm...

- VULNS: skill traits that double pre-roll magnitude. vuln:fire, vuln:physical, etc.; but also resist:physical...? maybe defense is always zero against effects with the given trait. a gaseous monster might even be vuln:area...!

hmm...

some ability trait ideas:

- physical: uses attack and defense
- spell: uses aptitude and resistance
- skill: uses focus v focus!!

elemental resists always apply when their trait comes up

*Whom the Earth Adores* is about the dragon's friend Drizket who proved unusually sociable, even playful, for an otherwise asocial manner of being. To dragon-kind, they were the emissary of the slimes. Knowing this, the troll king made an example of Drizket to assert dominance. Before he could be doused in dragonfire, he fled into the Dimdark, where he has remained for thousands of years, burrowing deeper into the heart of the world with his throng of loyal trolls. With Drizket's eyes, he wields powers over ancient slimes, and presses them into gruesome servitude. Those grim depths shake with the vicious wailing of the Troll King's faithful.

*Light and Hate* is about that dragon that freed the dragon from the dragon tamer. They fell in love, actually, and together laid a single egg. (What happened to the egg?) Then the empires of humankind went into upheavel, and droves were sent to slay dragons, equipped with terrible weapons. Lands that once accepted dragons as a part of reality now militarized against them, and each other, with greater viciousness than ever before. This other dragon died making a last stand, while the dragon escaped with the egg into the dimdark, where it proved inert despite careful tending. To lose a lover and a child, it is unimaginable grief. Please, retrieve Axuquatl's ashes. I would like to remember her one last time.

*'Til Death* is about the man that fatally wounded the dragon. He was a powerful wizard and artificer, and used enchanted automotons to defeat her majesty. He stole the egg and ground its petrified remains into a dust, to be used in some arcane manufacturing. Basically he started snorting it and it drove him insane, like so many other souls lost in the dimdark. The dragon advises the kobolds not to seek him out, as it would be even more than she could overcome. Nevertheless, the party takes up the quest. In the wizard's laboratory, they find the ani-crystal, which nullifies the dragon's wound and returns her to strength.

TODO
- Level up kobolds! Fake it to make it show up.
- [DONE] Equipment view: list equipment, allow to equip and disenchant.
- Crafting view: make items for 1 essence each, make equipment for ??? essence.
- Playground: pick 3 kobolds, fight the other three. kobolds "get tired" when they run out of health. does not yield experience.
- Lair view: choose to quest or delve
- Delve...!
- Quests...!

Armor changes:
- Increases defense, lowers initiative NO WAIT ARMOR IS FINE
- Only three kinds of armor, with different names for different tiers
- NO WAIT. SIX KINDS. three provide armor. three provide resists. (three provide both...???)

Phases of a turn:
- ... Each phase affects the creature
- Setup: Some effects tick, like poison, burning, bleeding, marked...
    - Aborts by returning nil
    - effects-tick
- Action Selection: Select action. (UI, AI really handles this)
- Pre-Action: Some effects apply, like hidden, empowered, extended.
- Target Selection
- Action: Final magnitude is computed into impacts.
- Expand Effects: Roll damage and healing, or other effects that use dice.
- Post-Action: Impacts update targets.
- Teardown: Passives with effects apply
