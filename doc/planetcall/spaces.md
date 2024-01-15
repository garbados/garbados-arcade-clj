# Planetcall: Spaces

Each hex on the world map is called a *space* and has various properties:

- Elevation (meters above sea level, effectively rounded to 500m increments)
- Rainfall (Arid, Moist, Rainy, Monsoon)
- Terrain (Flat, Rolling, Hilly, Mountaineous)
- Vegetation (None, Fungus, Forest)
- Feature (None, River, Xenobog, Ruin, Rare Metals, Thermal Vents)
- Weather (Miasma, Galvanized Hurricane, etc.)
- Improvement
- Units (up to 3)

Although all spaces have properties that assume they are above water, spaces with negative elevation are considered underwater. Underwater spaces come in a few types:

- Sea: less than one step below the surface and not mountaineous.
- Islands: less than one step below the surface and mountaineous.
- Ocean: greater than one step below the surface.

Islands can be treated as both land and water spaces, so aquatic and non-aquatic units can move through them normally. Improvements built on islands treat the space as though it were not underwater.

Some ideotech allows you to construct improvements on sea tiles.

## Rivers

A river acts as a road along adjacent river spaces, but it halts unit movement into or out of it for units that are not all-terrain or floating. This halting effect is nullified if there is a road on the space.

## Weather

There are two kinds of notable weather on Planet, miasma and galvanized hurricanes, which move around each turn.

- Miasma: Corrosive low-lying cloud formations. An impenetrable mixture of natural and synthetic vapors creeps across the world, eating through metals and composites. Units that end their turn in miasma take 3 damage. Driven by the motions of the sun, miasma will tend to move west across the world. (25% W, 25% NW, 25% SW, 25% no motion)
- Galvanized Hurricane (Arkhane): Electrified tornados that obliterate the land they touch. Arkhanes have a radius of 2 (diameter 5) with the center being a harmless "eye". Units that end their turn within the arkhane outside of its eye take 10 damage. Arkhanes move one space in a random direction for 3 turns before dissipating.