# Improvements

With construction actions, factions place improvements, which when yield and require resources. A faction may disable improvements, toggling whether they are operating. When you move a unit onto an improvement you don't control, you gain control of it and it is forcibly disabled. It may be re-enabled on the next turn.

Basic improvements:
- Farm: +1 food (+rainfall)
- Workblock: -1 food, +1 materials (+terrain)
- Reactor: -1 materials, +1 energy (+elevation)
- Lab: -1 energy, +2 knowledge
- Road: reduces the effective movement cost of the space to 0.
- Stockpile: can store up to 10 each of food, materials, and energy.

By default, improvements cannot be built over fungus or water. Alien units that move onto an improvement disable it as long as the unit remains there.

The yield of improvements is also affected by the underlying tile's qualities. Farms benefit from high rainfall and the presence of a xenobog, workblocks benefit from rockiness and the presence of rare metals, reactors benefit from high elevation and the presence of thermal vents, and labs benefit from the presence of ruins.

## Resource Stockpiling

The *Stockpile* improvement stores resources to fend off shortages. Resources other than knowledge can be stockpiled. If more of a resource is consumed than produced in a turn, it is drawn from the stockpile if able. Likewise, any surplus is contributed to the stockpile up to its limit.

## Workblocks and Unit Production

When a faction uses the Create Unit action, they pay materials to produce a unit on any of their claimed workblock improvements. The materials are drawn from stockpiles in that region, so that you may only produce units in regions with enough materials stockpiled already.

## Improvements and Unit Healing

Units that pass their turn restore 1 integrity, 2 if they are on an improvement other than a road, or 3 if they are on a workblock.
