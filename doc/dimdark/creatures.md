# Creatures

Kobolds and monsters are both creatures. Creatures have a *level*, as well as *attributes* and derived *stats*. Attributes grow as one's level increases, though this process is somewhat manual for kobolds and fully automatic for monsters.

## Attributes and Stats

Here are the essential attributes:

- Prowess: Physical strength and HP.
- Alacrity: Dodge and initiative.
- Vigor: HP and initiative.
- Spirit: magic and status resistance.
- Focus: magic and status effect strength.
- Luck: affects most things, but especially loot rarity.

Stats, which combine attributes with effects from statuses and equipment:

- HP: Prowess + Vigor + Level; damage a creature can soak before they collapse.
- Initiative: Alactrity + Vigor - Armor Weight; determines turn order.
- Dodge: Alacrity - Armor Weight; makes you harder to hit.
- Attack: Prowess; chance to hit.
- Defense: Armor Defense; lowers damage taken.
- Resistance: Spirit; DC for spells and effects.
- Aptitude: Focus; strength of spells and effects.
- Fortune: Luck; used when rolling loot.

In addition to resistance and aptitude, some gear will have element-specific values such as fire resistance or mental aptitude, per each element:

- Fire
- Ice
- Poison
- Mental

There are also *Merits* which confer elemental aptitude and resistance, but function like attributes:

- Scales: Fire
- Squish: Ice
- Stink: Poison
- Brat: Mental

## Statuses

Creatures may be affected by *statuses* -- temporary modifiers -- which are divided into *buffs* and *debuffs*:

- Buffs:
    - Attentive: Raises Aptitude.
    - Awakened: Adds the *Area* trait to the next skill used.
    - Empowered: Raises Focus significantly; removed by using a skill.
    - Hidden: Raises Dodge significantly; removed by using a skill or taking damage. Increases the effect of many Sneakthief skills.
    - Invigorated: Raises Attack.
    - Mending: Heals damage each turn, then less the next; stacks.
    - Prosperous: Raises Fortune.
    - Quickened: Raises Dodge.
    - Rallied: Raises Initiative.
    - Reinforced: Raises Defense.
    - Stalwart: Raises Resistance.
- Debuffs:
    - Bleeding: Inflicts damage each turn, as a proportion of current HP.
    - Burning: Inflicts fire damage each turn.
    - Chilled: Lowers Ice Resistance.
    - Cursed: Lowers Fortune.
    - Delayed: Lowers Initiative on the target's next turn.
    - Demoralized: Lowers Initiative.
    - Disarmed: Lowers Attack.
    - Distracted: Lowers Aptitude.
    - Exposed: Lowers Defense.
    - Frozen: Lowers Defense, Dodge, and Initiative significantly!
    - Marked: Dramatically increases the damage of the next attack to hit the marked creature.
    - Poisoned: Inflicts damage each turn, then less the next; stacks.
    - Provoked: Must target the provoker with their next action, or skip their turn.
    - Scorched: Lowers Fire Resistance.
    - Sedated: Lowers Mental Resistance.
    - Sickened: Lowers Poison Resistance.
    - Slowed: Lowers Dodge.
    - Withered: Lowers Resistance.

All statuses are cleared at the end of an encounter.

## Leveling Up

Creatures have a foundational *growth pattern* which indicates how much each attribute grows with each level. For primary attributes, this value will be one or greater, causing it to increase by a whole number with each level up. For secondary and tertiary attributes, this value may be a fraction of one, so the attribute only increases every few levels. As attributes apply to statistics only as whole numbers, these fractions are ignored except when leveling up.

Kobolds leveling up are also given three points to apply as the player sees fit. These points must be assigned to different attributes.

Monsters combine growth patterns from their species and class, so that monsters of the same species will still differ in attributes according to their class, while still sharing some traits.
