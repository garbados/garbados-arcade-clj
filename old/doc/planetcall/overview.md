Five vertices, each edge being a synergy strategy:

- Ecology (opposes industry, military) // The Green
- Science (opposes contact, industry) // The Academician
- Military (opposes ecology, contact) // The Warlord
- Industry (opposes science, ecology) // The Tycoon
- Contact (opposes military, science) // The Diplomat

Q:
- Why does Contact oppose Science? Because it does not value "the other".
- Why does Industry oppose Science? Because they are always at tension about "applications".

Edges:

- Ecology + Science: The Ark Steward
- Science + Military: The Calculator
- Military + Industry: The Engineer
- Industry + Contact: The Relic Hunter
- Contact + Ecology: The Planetcult

Tree shape:

- Each branch has four tiers. Taking tech in a branch blocks off the highest tier of its opposing branches, so taking Eco-1 blocks Ind-4 and Mil-4, while taking Ind-3 blocks Eco-2 and Sci-2.
- Tier 1 has only one choice: the definitive ideological starting point.
- Tier 2 has three choices per branch, and one choice per synergy branch.
- Tier 3 has four choices per branch and two choices per synergy branch.
- Tier 4 has five choices per branch and three choices per synergy branch.

Other notes:
- To access a tier you must have at least one tech from the previous tier within that branch, so Eco-3 requires at least one Eco-2. For synergy branches you must have at least one tech from each parent branch at a previous tier as well as at least one prior synergy tech if one exists. So Eco-Sci-2 takes Eco-1 and Sci-1 while Eco-Sci-3 requires Eco-Sci-2, Eco-2, and Sci-2.
- Some tech requires specific other tech because it improves upon it. You can't build a cruiser without understanding foils, for example, or a hovertank without a hovercraft.

Architecture of a Hex:
- Elevation (meters above sea level; prolonged eco-damage raises in 100m increments)
- Terrain (flat, rolling, hilly, mountaineous, coast, sea, ocean, atoll)
- Feature (None, River, Fungus, Xenobog, Forest, Ruin, City, Rare Metals Deposit)
- Weather (Miasma, Monsoon, Galvanized Hurricane, etc.)
- Improvement
- Units (up to 3 per hex)
- Yield considers terrain, feature, weather, and improvement.

Basic improvements:
- Vivarium: +1 Food
- Workblock: +1 Materials
- Reactor: +1 Energy
- Road: --

Cities:
- Habitability: Positive values provide yields, negative values impose upkeep.
- Population: Each pop may work one hex. A hex cannot be worked by more than one pop. Pops who do not work a hex can become specialists, roles enabled by tech. Improvements with upkeep that are worked by a city, apply that upkeep to the city. (Thus improvements do not apply upkeep when they are not worked.)
- Food / Cache / Growth: Food per turn is added to the cache, and contributes to progress toward the next pop. Negative food per turn takes from the cache, then is imported from other cities, and then contributes to pop degrowth (a threshold at which a pop is destroyed; ex: 100 growth, -100 degrowth).
- Range: hexes out to this distance can be worked by pops. Increases at 3, 7, 12, 18, and 25 (from a minimum of 1 up to a maximum of 6)
- Cities are centers of manufacturing and can queue up actions like the creation of improvements and units.
- Resource transportation:
	- When food is transported, it costs one energy per food. If there is a road connecting the source and target, transportation is free. The nearest cities are checked for a transportation cost, and the smallest is chosen until that city would produce no food itself, and then the next smallest is chosen, and so on. If all cities are exhausted 
	- Materials can be transported for free using a city action. The city action designates the target city.
	- Energy is never transported. It is considered global. At 0 energy, non-energy yields are halved.

Units:
- Units are designed in the Workshop menu. Designs represent a combination of a Chassis, a Loadout, and an optional Mod.
- Units have Movement, Arms, Resolve, Cost, and Upkeep attributes which are calculated from the unit's design. (Resolve will change as the unit engages in conflicts, representing a kind of experience and momentum. Defeat lowers resolve, victory increases it. Some Mods increase the influence of Resolve in conflicts.)
- Chassis dictates Movement, or the number of points available for movement across hexes each turn. Movement cost is determined by terrain. Roads take 1/2 movement.
- Loadout dictates Arms, which is used to determine Strength in martial conflicts. Loadouts may alternatively provide passive auras or activatable abilities.
- Mods can influence unit attributes (ex: reduce unit upkeep), unit movement (ex: reduce movement costs to 1), and martial conflicts (ex: +50% when attacking from shore), or provide activatable abilities (ex: Jump Jets).
- A unit's design dictates its cost, which is calculated by adding up the costs of the Chassis, Loadout, and Mod. Costs can be written as functions that are calculated using unit attributes, so that it is possible for a Mod's cost to scale off a unit's Movement.
- Up to three units can occupy the same space. During martial conflicts with adjacent units, the unit with the highest effective Strength defends. During conflicts at range, the attack rolls against all units in the hex.

Basic Designs:
- Chassis:
	- Infantry: 1 Movement.
- Loadouts:
	- Conventional Firearms: 1 Arms.
	- Colony Pod: Ability, *Settle*, founds a new City and destroys the Colony Pod.
- Mods:
	- None

Ideotech:
- Ecology:
	- 1. *Contamination Protocol*: Fungus does not affect unit movement. (Normally, Fungus costs all of a unit's movement points to cross.)
	- 2a. *Live Together*: Can build improvements on Fungus, and cities can construct Fungus features.
	- 2b. *Arcology Domes*: +1 Habitability.
	- 2c. *Planet-Tree*: Wonder, sinks 10 eco-damage, enables Ecologist specialists, which provide +3 Food, +1 Material, and sink 2 eco-damage. 
	- 3a. *Xenoherbalism*: Fungus yields +1 Food and +1 Knowledge. Xenobogs yield +3 Food, but you still can't build improvements on them. Xenobogs no longer affect unit movement.
	- 3b. *Elective Retrovirals*: +2 Habitability.
	- 3c. *Neutron Shielding*: Units no longer take damage from weather events like Miasma or Arckhanes.
	- 3d. *Spore Reactor*: Units and transports treat hexes with fungus as roads.
	- 4a. *Mycelial Transistor*: Fungus yields +1 Food, +1 Material, and +1 Energy.
	- 4b. *Breathing Free*: +4 Habitability.
	- 4c. *Extremophilic Composting*: Fungus sink 2 eco-damage.
	- 4d. *Bogsteel*: Xenobogs yield +3 Materials. Can construct xenobog features and construct improvements on xenobogs.
	- 4e. *Meteorological Planning*: Wonder, acts as unit that can create Miasma and Arkhanes on a cooldown.
- Ecology-Science:
	- 2. *Ark Directive*: Can construct forests, which provide +1 food and +1 materials.
	- 3a. *Hybrid Forest*: Forests sink 1 eco-damage.
	- 3b. *Carbon Sequestration*: Allows the construction of Carbon Battery improvements, which yield +1 Energy and sink 1 eco-damage.
	- 4a. *Ark Protocol*: Wonder, wincon. Construct another Ark ship and set it on a voyage to another world. 
	- 4b. *Shaper Guilds*: Forests sink 1 eco-damage and yield +1 Food, +1 Energy, and +1 Knowledge.
	- 4c.
- Science:
	- 1. *Scientific Method*: Allows the construction of Laboratory improvements that yield +2 Knowledge.
	- 2a. *Network Backbone*: Wonder, +10 Knowledge, enables Researcher specialist, which provides +2 Energy and +3 Knowledge.
	- 2b. *Archival Methodology*: Laboratories yield +1 Knowledge.
	- 2c. *Interface Deconstruction*: Allows the design of units using the Hacker loadout, who can stun units and steal Knowledge from cities.
	- 3a. *Photoreactive Shielding*: Units using the Hacker loadout cannot be detected except by adjacent units, and their ability cooldown is reduced by 1.
	- 3b. *Genejack Vats*: Workblocks provide +2 Materials.
	- 3c. *Plasma Helix*: Reactors provide +3 Energy.
	- 3d. *Autonomous Analysis*: Laboratories yield +2 Knowledge
	- 4a. *Dimensional Gate*: Wonder, wincon. Allows the empire to create a wormhole to other worlds.
	- 4b. *Replicators*: Cities produce +1 Food for each Energy produced.
	- 4c. *Cold Fusion*: Reactors provide +6 Energy
	- 4d. *Curious AI*: Laboratories yield +4 Knowledge.
	- 4e. *Singularity Collider*: Wonder, +20 Knowledge. Acts as a unit that can teleport units or destroy an enemy unit within range on a cooldown
- Science-Military:
	- 2. *Surveillance Net*: Allows the construction of Sensor improvements, which yields +1 Science and provides visibility and +50% Strength within 2 hexes to friendly units.
	- 3a. *Mind/Machine Interface*: Allows the design of units with the Spykit loadout, which renders them invisible except to adjacent units. Such units can convert enemy units on a cooldown.
	- 3b. *Jaeger Reactor*: Allows the design of units with the Walker chassis, which grants +5 Integrity, 3 movement, and reduces all movement costs to 1.
	- 4a. *Orbital Lasers*: Allows the construction of Laser Coordinator improvements, which when worked act as units that can call down an orbital strike within a range of 6.
	- 4b. *Doomsday Initiative*: Wonder, wincon. Develop a weapon so powerful it could destroy the planet, and then threaten to use it.
	- 4c.
- Military:
	- 1. *Chain of Command*: Allows the construction of units with the Command Module ability, which grants +50% Strength within 2 hexes to friendly units.
	- 2a. *Cataclysm Bunkers*: Allows the construction of Bunker improvements, which provide no yield but increase unit Strength by +100%.
	- 2b. *Hypersonic Magrails*: Allows the construction of units with the Railgun loadout, which provides +2 Strength.
	- 2c. *Plasma Artillery*: Allows the construction of units with the Discharger loadout, which provides +3 Strength, -5 Integrity, and +2 Range.
	- 3a. 
	- 3b. *Ferrofluid Projectors*: Railgun units have +2 Strength.
	- 3c. *Electrostatic Shockwave*: Discharger units have +2 Strength and +1 Range.
	- 3d. *Logistical Corps*: Wonder, reduces maintenance of all your units to 1 energy each.
	- 4a. 
	- 4b. 
	- 4c. 
	- 4d. 
	- 4e. 
- Military-Industry:
	- 2. *Doctrine: Mobility*: Allows the design of units with the Speeder chassis, which has 4 movement points.
	- 3a. *Doctrine: Flexibility*: Allows the design of units with the Cruiser chassis, which has 6 movement points and can only move on sea/ocean/atoll hexes and rivers. Cruisers can carry two land-based units, like infantry or speeders.
	- 3b. *Doctrine: Initiative*: Allows the design of units with the Needlejet chassis, which has 8 movement points and -5 Integrity, and all terrain costs 1 movement.
	- 4a. *Applied Gravitonics*: Allows the design of units with the Hovertank chassis, which has 4 movement points, +5 Integrity, and all terrain costs 1 movement.
	- 4b. *Nanomanufacturing*: Allows the design of units with the Fabricator loadout, which allows the unit to construct units, drawing from a base yield and their current tile's yield.
	- 4c. *Semiorbital Infrastructure*: Allows the design of units with the Skycopter chassis, which has 6 movement points, and all terrain costs 1 movement.
- Industry:
	- 1. *Exploitation Rationale*: Can construct Mine improvements, which yield +3 Material and produce 1 eco-damage.
	- 2a. *Pressure Domes*: Can construct Dome improvements, which yield +2 Habitability and produce 1 eco-damage.
	- 2b. *Borehole Mines*: Mine improvements yield +2 Material and produce 1 eco-damage
	- 2c. *Toxic Waste Regulation*: Reactors yield +3 Energy and produce 1 eco-damage.
	- 3a. *Daylight Protocol*: Domes yield +2 Habitability and produce 1 eco-damage. (Addition of fission reactors makes domes more self-sufficient, but they produce waste.)
	- 3b. *Mantle Drilling*: Mine improvements yield +2 Material and produce 1 eco-damage.
	- 3c. *Micromachine Nutrient Fixing*: Vivariums yield +3 Food and produce 1 eco-damage.
	- 3d. 
	- 4a. 
	- 4b.
	- 4c.
	- 4d.
	- 4e.
- Industry-Contact:
	- 2. *Relic Hunters*: Museums provide a unique bonus. Different ruins will provide different bonuses, which stack.
	- 3a.
	- 3b.
	- 4a. *Deepdrill Excavations*: May construct Ruin features, but this process produces 10 eco-damage.
	- 4b.
	- 4c.
- Contact:
	- 1. *Universal Regard*: Can construct Museums over Ruins, which yield +4 Knowledge.
	- 2a. *Diplomatic Corps*: Yields from treaties are increased by 50%. The reputation penalty for unthinkable tech is halved.
	- 2b. *United Factions*: Wonder, enables the Faction Congress. Players who construct it gain +2 votes on resolutions.
	- 2c. *Charter of Universal Rights*: +1 Resolve.
	- 3a. *Duty Standardization*: Yields from treaties are increased by 100%. Eliminates the reputation penalty for unthinkable tech.
	- 3b. *Archivist Guild*: Museums yield +4 Knowledge.
	- 3c. *Abolition of Scarcity*: +2 Resolve.
	- 3d. *Psi-Beacon*: Mod, doubles the effect of Resolve on Strength for both attacker and defender, while halving the effect of Arms.
	- 4a. *Secrets of the Planetcall*: Wonder, wincon. Discover who built the Beacon, and why.
	- 4b. *Eudaimonia*: +4 Resolve.
	- 4c. *Psych Corps*: Enables the design of units with the *Psycher* Loadout, which grants the ability to drastically raise or lower a unit's Resolve at a distance of 2.
	- 4d. 
	- 4e.
- Contact-Ecology:
	- 2. *Planetcult*: Allow the construction of Planet-Temples, which sink 3 eco-damage when worked.
	- 3a. *Exodiplomacy*: Native fauna will not attack your units.
	- 3b. *Voice of Planet*: Planet-Temples sink 2 eco-damage and provide +1 food and +1 knowledge.
	- 4a. *Alliance with Planet*: Native fauna are considered allied units and your units may stack with them.
	- 4b. *Stronger Together*: Fungus squares worked by your cities have a chance (25%) of spawning a native fauna unit each turn.
	- 4c. *Ascent to Transcendance*: Wonder, wincon. Grant total awareness to the planet itself, and become a part of that consciousness.

------

Six vertices, each edge being a synergy strategy:

Vertices:
- Ecology (opposes industry)
- Science (opposes worship)
- Contact (opposes military)
- Industry (opposes ecology)
- Military (opposes contact)
- Worship (opposes science)

Edges:
- Planetcult (ecology-worship)
- Ark Steward (ecology-science)
- Exodiplomat (contact-science)
- Excavator (contact-industry)
- War Machine (industry-military)
- Deus Vult (military-worship)

Strategies:
- Decolonizer (Planetcult): Ecology / worship. Benefit from native terrain features (fungus, miasma, xenobog), increasing their yield and providing combat bonuses. Turns alien units friendly and promotes their spawning on native terrain. Infrastructure improves habitability and reduces eco-damage, making it easy to build a wide empire. Spawning alien units makes the world much more hostile for industrial factions, and terrain advantages make it difficult to go to war against the decolonizer. Wincon: mind-flower (massive improvement), allied victory (subdue or destroy the colonizers)
- Ark Steward: Ecology / science. Spread terrain like forests that displace fungus without causing eco-damage, and improvements like carbon batteries that absorb eco-damage, as well as solar arrays that generate energy. The rich science and energy yields of these improvements makes the ark steward a weak military power but a strong economic one. High habitability amid heavy infrastructural requirements foster cultivating a few powerful cities. Wincon: build another ark ship (massive improvement)
- Scientist: Pure science. Uses advanced improvements, chassis, and loadouts to cultivate sophisticated situational tactical advantages. Later technologies create a knowledge-for-its-own-sake ethic, represented by genejacks and espionage. The scientist can make their way as a single powerful and well-defended city, as going wide increases technology requirements. Wincon: dimensional gate (massive improvement)
- Diplomat (contact): turn alien ruins into museums and improve the yields of diplomatic alliances (ex: research agreements provide more science, economic treaties provide more energy as well as food and materials). Through careful command of realpolitik, the diplomat becomes a kingmaker that relies on political connections to support a powerful economy. To defend itself it relies on military treaties. Wincon: alien beacon (massive improvement), electoral victory (become UF [united factions] president)
- Scavgineer (contact-industry): turn alien ruins into powerful unique improvements. The geographic distribution of alien ruins incentivizes a wide empire, with these unique abilities making even basic units into potent forces.