# Units

Units represent organized forces, such as infantry or engineering corps. They are built according to *designs* which specify a *chassis*, *loadout*, and up to two *mods*. Chassis determine a unit's Speed and loadouts determine its Arms, while mods can grant passive traits and active abilities. Chassis and loadouts may have passive traits of their own, and loadouts sometimes include active abilities. In total, a unit ends up with these properties:

- Arms: Martial strength. Applies in [combat](./combat.md).
- Resolve: Morale and experience. Applies in [combat](./combat.md).
- Speed: Number of movement points to be spent on movement across tiles each turn.
- Traits: Passive effects that may apply to the unit or within a range. *A unit may acquire temporary traits from attacks or other effects.*
- Abilities: Activatable effects that can be applied on a cooldown of one or more turns. This may include special attacks that apply negative traits.
- Cost: Total materials required to produce the unit. Determined by the unit's associated design (though some mods lower cost!).
- Upkeep: Energy per turn required to maintain the unit. Determined by the unit's cost (though some mods lower upkeep!).

## Traits

- All-terrain: Movement costs for this unit are always 1 or less.
- Aquatic: Unit may move on water tiles but not land tiles.
- Arbiter: Other units within a range of 2 are *cloaked*.
- Arkhanist: Unit produces energy and knowledge the closer it is to the eye of an Arkhane. (Usually accompanies Shielded.)
- Artillery: Unit is carrying equipment for heavy bombardment. If confronted, it is considered to have 0 Arms.
- Automated: Reduce the unit's cost by 25% but increase its upkeep by 50%.
- Clean: Increase the unit's cost by 25% but reduce its upkeep to 1. If it is already 1, reduce it to 0.
- Cloaked: Unit is only visible to other factions if they have treaty 3 or greater, or if they have an adjacent unit, or if they are working a *sensor* improvement in range of it, or if a *surveillor* unit is within range.
- Commander: Grants advantage to friendly units within a range of 2.
- Embarked: Unit is being transported by another unit. It is not considered when selecting defenders, and will be destroyed if the transport is destroyed. Unit gains the ability *Disembark* which allows them to move independently again. Disembarking removes the *Embarked* trait.
- Engineer: Unit has access to active abilities with the *engineering* trait, including the creation of roads and the destruction of improvements.
- Floating: Unit may move over land or water tiles.
- Flying: A flying unit is a stationary unit that may bombard units as far out as its chassis' *Speed*. It may choose to *defend* in which case it will automatically confront other flying units that attempt to bombard within its range. Flying units may *rebase* to move between your cities or units with the *transport* trait.
- Fortified: +5 Integrity.
- Frail: -5 Integrity.
- Psi-field: Friendly units within a range of 2 are granted the *psychic* trait while they are in range.
- Psychic: During confrontations with this unit, the influence of Arms is halved and the influence of Resolve is doubled for both the attacker and the defender.
- Reach: Unit will counterattack when bombarded by flying units.
- Shielded: Unit does not take damage from weather effects.
- Stunned: Unit cannot counterattack. During its turn, it does nothing. All stuns are temporary and have an associated duration. Stunning a unit that is already stunned increases the original stun's duration by the new stun's duration.
- Surveillor: Unit can detect cloaked units within a range of 2.
- Transport: Unit is capable of transporting other units in its stack, such as a ship transporting land units across the sea. When confronted, transported units are not considered for defense. Units may *board* the transport or *disembark* to leave its tile. (Without a transport, land units embarked at sea are considered to have an effective strength of 0 and a speed of 2.)
- Trooper: Unit has advantage 2 against native fauna.

## Abilities

- Create Road: Creates a *road* improvement on the occupied tile. Cooldown: 3. Engineering ability.
- Create Fungus: Creates *fungus* vegetation on the occupied tile. Cooldown: 3. Engineering ability, with appropriate ideotech.
- Create Forest: Creates *forest* vegetation on the occupied tile. Cooldown: 3. Engineering ability, with appropriate ideotech.
- Destroy Vegetation: Removes vegetation on the occupied tile. Cooldown: 3.
- Destroy Improvement: Removes an improvement on the occupied tile. Cooldown: 3. Engineering ability.
- Disable: Stun an adjacent unit stack for one turn. Cooldown: 3.
- Subvert: Place an adjacent enemy unit under your control for one turn, or cause a unit in a stack to confront its allies. Cooldown: 4.
- Combust Miasma: Remove *miasma* weather from the current tile to grant advantage 2 to units within a range of 2 for 2 turns. Cooldown: 3.
- Create Miasma: Create *miasma* weather on the occupied tile. It will drift with prevailing weather currents. Cooldown: 3.
- Heavenfall: Unit makes an orbital insertion to a tile within a range of 16. Uses all of the unit's movement points.
- Bombard: Unit bombards an enemy stack within range. See [combat.md](./combat.md) for more information.
- Confront: Make hostile contact with an enemy stack. Moving onto an enemy stack automatically initiates confrontation. See [combat.md](./combat.md) for more information.
- Psychout: Raise the resolve of a friendly unit by `2 + (this unit's resolve / 2)`, or lower an enemy unit's resolve by that much. Cooldown: 2.

## Chassis

- Infantry: 2 Speed.
- Speeder: 4 Speed.
- Foil: 4 Speed. Aquatic, Transport.
- Cruiser: 6 Speed. Aquatic, Transport, Fortified.
- Hovercraft: 4 Speed. Floating, Frail.
- Hovertank: 4 Speed. Floating, Fortified.
- Jaeger: 4 Speed. All-terrain, Fortified.
- Needlejet: 8 Speed. Flying, Frail.
- Skyfort: 6 Speed. All-terrain, Floating, Transport.

## Loadout

- Firearms: 1 Arms.
- Engineering Module: 0 Arms. Engineer.

## Mods

TODO list mods
