# Skills

Skills have *traits* and *effects*. Traits describe what stats are relevant to resolving the skill when used, as well as properties like how many targets the skill affects, whether you must be *close* or *distant* to use the skill, etc.; effects are imperative functions that do something like roll damage or apply buffs and debuffs.

Skills may also have a *cooldown* which refers to the number of turns the skill must spend recharging before it can be used again. The default cooldown is zero.

In general, skills go through this check when used:

- Subtract relevant target stat from relevant user stat.
- Apply log2 to the result.
- Subtract the result from 10. This is the *threshold*.
- Roll 3d6. If greater than the threshold, skill hits. The difference is the *magnitude*.
- Skill applies its effects, which may scale with the magnitude.

*N.B.: Friendly skills do not subtract a target stat.*

Some skills go through this check multiple times, such as an attack that tries to apply a debuff when it hits. In that case, the attack rolls to hit, and then the effect rolls to apply the debuff.
