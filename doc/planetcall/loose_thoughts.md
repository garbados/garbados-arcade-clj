# Thoughts

Improvements from Ideotech:
- Condenser: -2 energy, +2 adjacency bonus to Farms
- Borehole: -2 energy, +2 adjacency bonus to Mines
- Refractor: -2 materials, +2 adjacency bonus to Reactors
- Academy: -2 energy, +2 adjacency bonus to Labs
- Nanofab: -2 energy, -2 materials, +2 adjacency bonus to factories
- Sensor: -1 energy, vision and advantage within radius 2
- Bunker: -1 materials, advantage 2 for units in this space
- Detoxer: -1 energy, +5 harmony
- Embassy: -1 food, +1 treaty income

Wonders:
- (wonders have an upkeep of -2 energy)
- Empath Guild: +5 treaty income, +1 adjacency bonus to embassies.
- Atmospheric Condenser: +2 adjacency bonus to farms, gain society ability to create weather events.
- Terraforming Protocol: Engineering ability cooldowns are reduced by 1.
- Tac-Com: Sensors, bunkers, and command modules provide +1 advantage within +1 radius. Sensors provide +1 vision.

Fauna:
- Lopers: Packs of jaguar-like reptiles, capable of decimating ill-equipped squadrons.
- Wormswarm: A writhing mass of aggravated psychic tentacles, the fungus' animate defense mechanism.
- Razorbeak: A quadruple-winged megafauna, capable of carrying off vehicles.
- Bloodgnats: Swarms of large buzzing insects, capable of dessicating their prey. Awakens from hibernation when the world suffers from medium eco-damage.
- Oathwyrm: An ancient alien, possibly robotic. Emerges from underground to ravage the land at random intervals.
- Draconauts: Enormous sea-snakes that wriggle through the ocean. Vast beasts with a distinct indifference toward the agents of empire.
- Bowerholm: Sedentary turtles the size of countrysides. They emerge from hibernation when the world suffers from low eco-damage; as seas rise, islands reveal themselves to have been Bowerholms all along.
- Curators: Aged robotic remnants of past civilizations. At best, a liability. At worst, a threat. (moves randomly, confronting if moving there would cause it.)
- Infested Curators: Rogue robots that native fungus has gained control over. Appears only while the world is suffering from high eco-damage. (Curator stats, but they emerge from fungus frequently and act under the hidden player's control.)
- Troll: Giant that inexplicably tends the fungus. Heals all integrity damage if it ends its turn on fungus. Does not move off of fungus squares.
- Hewer: Massive, aggressive beast with razor limbs. Sleeps under the fungus until the world suffers from high eco-damage. (Slow, deadly, high integrity)

Curiosities, aka resource pods:
- (object that causes an event when captured, then disappears)
- Gain a large resource surplus this turn.
- Gain a unit of strange design.
- Gain experience.
- Builds an alien improvement.
    - Borehole
    - Condenser
    - Reflector
- Reveals a feature:
    - Xenobog
    - Rare metals
    - Thermal vents
    - Ruins
- Spawns hostile fauna.
- Spreads Fungus

Engineering units:
- Abilities on cooldowns, not spend-turns-constructing.
- Cooldowns are generally 3 turns.
- Ability applies to the occupied space.
- Basic abilities:
    - Remove Fungus
    - Construct Farm
    - Construct Workblock
    - Construct Reactor
    - Construct Lab
    - Construct Factory
    - Construct Road
    - Construct Stockpile
    - Construct Wonder (only if player could construct that wonder (so industrialist can't complete your mind flower at the last second))

Map & Visibility:
- Spaces that have not been explored are blacked out.
- Spaces that have been explored but which are not currently visible, are greyed out.
- Spaces do not update for that player until they have been viewed again.
- Spaces that are *observed* (have an observer like a sensor) gain a yellow border.
- Improvements provide vision 1.
- Units provide vision 2.
- Sensors provide vision 2.

Arkship:
- Rather than species stats, all factions start with one ideotech representing their shared origin.
- Factions start with another tech representing their ideology.

Starting tech:
- At the center of the ideotech blossom is *tech zero* which all players possess when the game begins. It grants:
    - The infantry and foil chassis.
    - The engineer and firearms chassis.
    - Name TBD (*Scars of a Broken World*?)

Defeat:
- A player loses when they control no more improvements or units.

Treaties:
- Five levels of treaties:
    - -1: Vendetta!
    - 0: Neutral (no treaty income)
    - 1: Truce (treaty income 1x)
    - 2: Pact (treaty income 2x, join defensive wars)
    - 3: Alliance (treaty income 3x, units may stack, share victory conditions)
- You may move down one level of treaty once per turn.
    - Moving from Alliance to Pact may shunt friendly units around to ensure they no longer stack.
- You may propose to go up one level of treaty on your turn. Target will accept / reject on their turn.
- Treaty income is 10% of gross yields per level. Thus, an alliance grants 30% of each party's gross yields to the other.
- Reputation improves treaty income by 5%, cumulative with treaty level. Thus a truce and two embassies provides a 20% share of your partner's gross yields. Reputation provides half this bonus to the other party in treaties.
- Income from treaties pays for upkeeps last, and ignores improvement groups.

Planetary Council:
- Once the *Faction Assembly* wonder had been constructed, players may convene the Planetary Council. Council conventions have a cooldown of ten turns, or five for whoever constructed the wonder.
- A variety of measures, some of which are repeatable, can be selected by whoever called the convention. They may select one measure to vote on per convention.
- Each player gets one vote on the measure, plus 1 per 5 reputation, plus 2 if they constructed the *Faction Assembly*.
- Measures:
    - Ecological Sanctions: Players gain -1 reputation per 4 eco-damage produced. Repeatable.
    - Peace Accords: Players gain -2 reputation for each other player they have a vendetta against. Repeatable.
    - Atrocity Protocols: Whenever a player constructs a unit with the *atrocity* condition, all their treaties are reduced by 1 level to a minimum of 0.
    - TODO

## Core Ideologies

Science:

- 1:
    - Scientific Method: Labs produce +1 knowledge. 
    - Doctrine, Observation: Engineers may construct Sensors (-1 energy, provides vision and advantage 1 within radius 2)
- 2:
    - Higher Education: Engineers may construct Academies, which provide +2 knowledge for adjacent labs.
    - Interface Deconstruction: Designs may include the Hacker loadout, with the ability to stun stacks of units.
    - c:
- 3:
    - a: 
    - b:
    - c:
    - d:
- 4:
    - a: 
    - b:
    - c:
    - d:
    - e:

Ecology:

- 1:
    - Doctrine, Pathfinding: Units may move over spaces with fungus normally. 
    - Exoshielding: Units take no damage from miasma.
- 2:
    - Virtue, Integration: Engineers may construct improvements on spaces with fungus. Improvements are not destroyed if fungus expands onto their spaces.
    - Restoration Protocol: Engineers may construct Detoxers (-1 energy, +5 harmony). Designs may include a Shielding mod, which nullifies damage from weather and radiation.
    - c:
- 3:
    - Mycelial Interlink: Units treat spaces with fungus as roads. Spaces with fungus are considered roads when calculating upkeep and yields. Engineers gain the Plant Fungus ability.
    - Living Cities: Improvements on spaces with fungus gain +1 harmony and +1 yield.
    - Miasmic Fission: Units restore integrity if they end their turn in miasma. Engineers may construct miasma.
    - d:
- 4:
    - Virtue, Symbiosis: Units on or attacking into spaces with fungus gain advantage 2. Improvements on spaces with fungus gain +1 harmony and +1 yield.
    - Miasmic Fusion: Units on or attacking into spaces with miasma gain advantage 2. Units that end their turn in miasma are considered *cloaked*.
    - c:
    - d:
    - e:

Contact:

- 1:
    - Universal Regard: Engineers gain the Construct Embassy ability (-1 food, +1 reputation).
    - It Belongs In A Museum: Improvements on Ruins provide +2 knowledge.
- 2:
    - United Factions: Engineers may contribute to the construction of the *Faction Assembly* wonder, which enables the Planetary Council, provides +2 votes on measures, and +3 reputation.
    - Charter of Universal Rights: Your units enter play with +1 resolve.
    - Guestright: Gain +2 reputation. Embassies provide +1 reputation.
- 3:
    - Practical Psi: Designs may use the Psi-Beacon loadout, which decreases the effect of Arms on Strength by 50% and increases the effect of Resolve by 50%. Designs may use the Psi-Beacon mod which has the same effect. (A unit with both nullifies the effect of Arms in direct confrontation.)
    - b:
    - c:
    - d:
- 4:
    - a: 
    - b:
    - c:
    - d:
    - e:

Industry:

- 1:
    - Acquatic Engineering: Engineers may construct improvements on sea tiles. Improvements are not destroyed when the space becomes an island or sea, but are still destroyed if it becomes an ocean.
    - Exploitation Rationale: Improvements provide +1 yield but produce 1 eco-damage.
- 2:
    - Doctrine, Plunder: Engineers may construct Mantle Boreholes, which produce energy and materials, and significant eco-damage.
    - b:
    - c:
- 3:
    - a: 
    - b:
    - c:
    - d:
- 4:
    - a: 
    - b:
    - c:
    - d:
    - e:

Military:

- 1:
    - Chain of Command: Designs may include the Commander mod, which grants advantage 1 in radius 2.
    - Superior Firepower: Designs may include the Heavy Arms mod, which grants +2 arms.
- 2:
    - Permanent Fortifications: Engineers may construct Bunkers, which provide advantage 2 to units on the space.
    - Advanced Armaments: Designs may include the Railguns loadout, which has 2 arms, or the Shock Battery loadout, which has 3 arms and the *artillery* condition.
    - Specialized Munitions: Designs may include the Radioactive Slugs mod, the Fissile Engine mod, and the Anti-Material Ordinance mod.
- 3:
    - Ferrofluid Projectors: Designs may include the Magspears loadout, which has 3 arms, and the Plasma Discharger loadout, which has 5 arms and the *artillery* condition.
    - Orbital Rocketry: Engineers may construct Missile Bases (-1 energy, may bombard at a significant range on a cooldown). Designs with the *artillery* condition may use the High-Orbit Mortar mod, which increases their range by 1.
    - Virtue, Discipline: Engineers may contribute to the construction of the Tac-Com wonder, which increases the radius and effect of Sensors, Bunkers, and the Commander mod.
    - d:
- 4:
    - Wavelength Disruption: Designs may include the Disintegrator loadout, which has 4 arms, and the Annihilation Arc loadout, which has 7 arms and the *artillery* condition.
    - b:
    - c:
    - d:
    - e:

## Synergy Ideologies

Science-Military:

- 1:
    - a: 
    - b:
- 2:
    - a: 
    - b:
    - c:
- 3:
    - a: 
    - b:
    - c:
    - d:

Military-Industry:

- 1:
    - Scorched Earth: Designs may use the Incinerator mod, which doubles Strength against fauna and units with a fauna chassis. The cooldown for the Remove Fungus engineering ability is reduced by 1.
    - Doctrine, Mobility: Designs may use the Speeder chassis, which has speed 4.
- 2:
    - a: 
    - b:
    - c:
- 3:
    - a: 
    - b:
    - c:
    - d:

Industry-Contact:

- 1:
    - a: 
    - b:
- 2:
    - a: 
    - b:
    - c:
- 3:
    - a: 
    - b:
    - c:
    - d:

Contact-Ecology:

- 1:
    - Planetcult: Embassies provide +2 harmony.
    - Exodiplomacy: Native fauna will not attack your units. Fauna do not disrupt your improvements when they move onto their spaces.
- 2:
    - Voice of Planet: Embassies provide +1 knowledge and no longer require upkeep.
    - Pax Planeta: Native fauna are considered allied units, so your units may stack with them peacefully.
    - c:
- 3:
    - Ascent to Transcendance: Engineers may contribute to the construction of the *Mind Flower* wonder, which acts as a centralized intellectual interface for the global fungal network, allowing it to communicate with your people as a conscious entity. (Building this wins the game.)
    - b:
    - c:
    - d:

Ecology-Science:

- 1:
    - Ark Directive: Engineers gain the Plant Forest ability. Forests improve a space's yield by +1. 
    - Net-Positive Sequestration: Reactors gain +1 harmony and +1 energy.
- 2:
    - Proactive Meteorology: Engineers may construct Condensers (-2 energy, +2 yield for adjacent farms).
    - Hybridization: Improvements adjacent to fungus or forests gain +1 yield, or +2 if adjacent to both.
    - Domestication: Designs may use the *Loper* and *Wormswarm* chassis.
- 3:
    - Doctrine, Stewardship: Spaces with vegetation on them gain +1 harmony and +1 yield. Spaces with forests on them gain an additional +1 yield.
    - Applied Mutagenics: Designs may use the *Troll* and *Razorbeak* chassis.
    - Ark Protocol: Engineers may contribute to the construction of the *Dimensional Gate* wonder, which creates artificial wormholes to other planets. Your empire then uses them to collate and preserve artifacts from hundreds of words, in keeping with the ethos of an ark ship. (Building this wins the game.)
    -  Weather Control: Engineers may contribute to the construction of the Atmospheric Cortex wonder, which grants +2 yield to adjacent farms, and societal abilities to create and destroy weather. 