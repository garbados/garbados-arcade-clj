# How to Play

First, each player selects a landing zone. The game selects a landing region for each player so that there are no overlaps, and then prompts each player to select a hex within their region. This hex begins improved with a *stockpile* and claimed by that faction. A basic unit is also created at this location, allowing you to explore.

Turn order is decided once, at random, after landing zone selection.

In order, players take turns until the game ends. The first player goes, and then the second, and so on, back to the first, and over again. During a turn, there is a *gathering phase*, when space and treaty incomes are accounted for; an *action phase*, during which the player takes actions such as the modification of spaces or the creation of units; and a *movement phase*, when units are moved about. Combat only takes place during the last phase.

The game can end one of several ways:

- All players control no spaces: The dangers of Planet consume all comers.
- Only one player controls spaces: An empire of empires seizes the world for itself.
- Eco-damage reaches *cataclysmic* levels: Toxicification renders Planet strictly uninhabitable.
- A player constructs a transformative wonder: A new era dawns by some mortal hand.

How the game ends will determine the concluding narrative, and whether or not a player can be said to have won.

## Basic Concepts

- Space: a position on the map and its qualities, such as terrain and rainfall.
- Claimed: whether a space is controlled by a player, and which player controls it.
- Region: a set of contiguous claimed spaces; improvements exchange resources within regions.
- Resources: food, materials, energy; accumulated by most improvements, stored in stockpiles.
- Improvement: a productive system that occupies land, whether a farm, laboratory, etc.
- Wonder: a special improvement with profound effects, which is placed in stages.
- Faction: a splinter of the arkship's crew, united around a founding ideotech.
- Ideotech: ideological technologies; rationales that yield material advances.
- Unit: an equipped and mobile squadron, built from a design; usually imposes upkeep.
- Upkeep: a cost, in energy, that must be paid each turn to keep a unit fit for duty.
- Design: a loadout, a chassis, and up to two mods.
- Loadout: a unit's primary equipment, usually a type of armament.
- Chassis: a unit's general formation, whether infantry, a speeder, a mecha, etc.
- Mods: unit modifications which confer traits and abilities.
- Traits: qualities with effects, such as whether a unit is "heavy" or "hidden".
- Abilities: unit abilities, such as bombardment, which used during its faction's *movement phase*.
- Turn: a player's turn, consisting of a gathering phase (automatic), an action phase, and a movement phase.
- Action: a faction-level decision, such as placing an improvement or building a unit, which are taken during the *action phase*.
- Planet: this ruined ecuminopolis; the 0th player, which controls all wildlife.
- Wildlife: organisms native to Planet; units belonging to the 0th player.
- Ecodamage: generalized damage to the ecological systems of Planet; may reach different levels of cataclysm; caused by placing improvements, some ideotech, etc.
- Impact: the consequences, in ecodamage, of an improvement each turn, an action, or an ability.
- Cataclysm: stages of dehabitabilization, the toxicification of Planet's biosphere; imposes increasing penalties to yields from improvements, until habitation is simply no longer an option.

## Player Turns

### Gathering Phase

At the beginning of a player's turn, their improvements yield and consume resources. In order, yields and upkeeps are tallied:

- Trade
- Food
- Materials
- Energy
- Knowledge
- Upkeep

Treaties provide trade yields which grant resources of all kinds equal to their level, before any other costs are considered. These resources are distributed equally among all stockpiles, regardless of region. See [*Treaties*](./treaties.md) for more information.

Yields and upkeeps from improvements are homed to regions. If yields of a given resource exceed upkeeps in a region, the surplus is divided among local stockpiles equally, up to their storage limit. If the surplus exceeds the collective available storage of local stockpiles, the difference is lost. If yields do not cover upkeeps, the difference is drawn from local stockpiles. If local stockpiles cannot pay, a shortage affects all subsequent resource gathering steps in the phase, halving yields. A food shortage hampers everything after it. An energy shortage affects knowledge production and weakens your units. An upkeep shortage only weakens your units.

Unit upkeeps are not homed to a region, and thus are drawn from the stockpiles and surpluses of a faction's regions: first from total surplus, then from total storage. Thus, if one region could afford all unit upkeeps, despite an energy deficit in other regions, then unit upkeeps would still be satisfied.

After resources are dealt with, the game assesses the ecological impact from improvements. Then, finally, knowledge accumulates as research progress, potentially completing the current research pursuit.

The gathering phase is automatic, but may generate three types of notifications:

- Shortage, including resource and any affected regions.
- Spillover -- a loss of resources for want of storage -- specifying the resource and the affected stockpiles.
- Needs new research pursuit, including what was just completed.

### Action Phase

Each turn, a player may take a number of actions such as constructing improvements or vegetation, or marshaling units. By default this number is *two*, but certain ideotech add more actions per turn -- or take them away!

Construction actions can be used to claim unclaimed spaces. You can construct an improvement on an unclaimed space adjacent to any of your claimed spaces, thus claiming it, or on a space you have already claimed, thus replacing the improvement already there. However, be warned: constructing on unclaimed spaces causes eco-damage!

Players begin the game with some actions, and gain others by researching ideotech:

- Construct {basic improvements}: Factions begin play with actions to construct all basic [improvements](./improvements.md).
- Marshal Unit: Create a new unit on a space with a *workblock* improvement. Units cost materials to produce -- materials that are drawn from stockpiles local to the workblock's region -- and energy per turn to maintain; these values are drawn from their design. (See [Units](./units.md) for more information.)

### Movement Phase

Once a player has selected their actions for the turn, they progress to the movement phase. Each movement point of each of the player's units must be accounted for. Unit abilities usually consume movement points.

Chassis grant abilities for moving around, such as:

- Travel: Use movement points to cross land spaces. The movement cost of a space is equal to half its rockiness, rounded down, plus one. Fungus increases movement cost by two. Cannot be used on aquatic spaces. Units with fewer movement points than the cost of moving into a space, they may spend all their remaining movement points to move into it.
- Embark: Use movement points to cross aquatic spaces. The movement cost of aquatic spaces is one, or three if the space has fungus. Cannot be used on land spaces. Units with fewer movement points than the cost of moving into a space, they may spend all their remaining movement points to move into it.
- Hover: Use movement points to cross any type of space at a constant movement cost of 1 per space crossed.
- Intercept: Consume all movement points to counter aerial attacks within N spaces, where N is the number of movement points consumed.

Loadouts grant offensive and tactical abilities, such as:

- Attack: Confront an adjacent unit.
- Bombard: Bombard a stack of units within range.
- Strike: Commence an aerial assault against a unit within N spaces, where N is the unit's speed.

Typically, offensive abilities consume all a unit's remaining movement points. Some unit mods change this. (*See [Combat](./combat.md) for more information.*)

All units share some abilities:

- Rest: Consume all movement points, healing some integrity if possible.
- Fortify: Consume all remaining movement points to gain a defensive bonus.

Once all the player's units have consumed all their movement points, their turn ends and passes to the next player.