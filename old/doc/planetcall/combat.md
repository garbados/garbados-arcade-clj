# Combat

## Stacking

Up to three units may share a space, forming a *stack*. If there are three units in a space, it is an invalid space for others to enter. Some abilities affect whole stacks, or function differently on lone units versus a stack.

## Confrontation

Units belonging to factions at war may engage each other in mortal conflict, in a process known as *confrontation*, during which a unit attempts to damage another. Confrontations balance around a measure known as *strength*, which is calculated from each unit's *arms* and *resolve*.

In combat, units compare strength in order to determine damage. Here is a basic formula for unit strength:

- `strength = ((arms * a) + (resolve * b)) * (1 + (advantages / 4))`

The variables `a` and `b` reflect the relative weight of arms and resolve on the unit's strength. As such, they are usually 1 and 1/2, but traits and abilities sometimes affect this. For example, psi powers tend to increase the impact of resolve while reducing the impact of arms.

Advantages represent distinctive tactical boons, such as the presence of a commander or beneficial terrain. A negative advantage is considered a *disadvantage*; some abilities apply such. Disadvantages and advantages negate each other one for one.

To determine damage, subtract the defender's strength from the attacker's and roll 2dN where N is the difference, rounded down, to a minimum of 1. (This means that an attack will always deal at least 2 damage.) Thus, if a defender has 5 strength and the attacker has 3, the attack will deal 1 damage. If a defender has 3 strength and the attacker has 6, the attack will deal 2d3 damage.

Damage is subtracted from a unit's integrity. At 0 integrity, the unit is destroyed.

When a unit is destroyed, the resolve of nearby allied units goes down by 1. When an enemy unit is destroyed, the resolve of nearby allied units goes up by 1. Negative resolve will trend up to 1 in steps of 0.5, so demoralized units can slowly recover their morale. Resolve above 3 will trend toward 3 in steps of 0.5, so high-morale units lose their bluster over time.

The three confrontational abilities use these calculations in different ways:

- Attack: Targeting a space containing one or more enemy units, select the highest-strength unit in that stack as the defender. The attacker confronts the defender, then the defender confronts the attacker, and finally the attacker confronts the defender again; this pattern is also referred to as *attack-counter-attack*.
- Bombard: Targeting a space containing one or more enemy units, confront each of them once.
- Strike: Targeting a space containing one or more enemy units, determine whether there are any units in that space with *reach*, or if there are any aerial units *intercepting* within range of the space. If there are, the highest-strength one is chosen as defender; the attacker confronts the defender once, then the defender confronts the attacker. Otherwise, *strike* acts as *bombard*.

As it was mentioned, here is the rule for *intercept*:

- Intercept: If any friendly units within N spaces of this unit, where N is its speed, are targeted by *strike*, this unit may be chosen as defender.
