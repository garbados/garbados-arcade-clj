# NOTES

A running, disorganized dev log. Like a changelog but for ideas.

----

been playing ZEPHON lately and getting the planetcall itch

Cities
- Emergent property of industrial specialization.
- ZEPHON combines space and development by having cities claim tiles as a form of work, making borders an enforced matter, and possible density a matter of development.
- What if borders were emergent, rather than encoded, so land can be *stolen* -- but why assume transferring development makes sense? Why would {industry} show up to an {ecology} factory and just... get it? Why would I think {industry} could work an {industry-military} factory, even if they're ideologically similar? What does ideotech *really* have to do with the mix of techno-cultural factors that drives standards and practices? In ZEPHON, cities can *only* be destroyed to the last. They just explode.
    - But Gaza is a ruin. A city is not real. One has been made unreal.
- What if
    - developing a tile that is adjacent to a worked tile is an action
    - building on a tile is a separate sort of action. "expanding" a tile is like upgrading it, adding a building slot?
    - "claiming" a tile? is a thing? like "hey i call dibs, but i don't work it" ; maybe a diplomatic basis for face games
    - there's a tier-0 (ideo-neutral) tech to build "settlers" which creates a unit that can deploy to become a worked tile.
    - what if instead of determining for partitioned economies, one gets a bonus if their territory is contiguous? this allows working remote features, simplifies calculation, and incentizes smaller realms. domains?

Turns
- What if turns are simultaneous? like Diplomacy? so units can bounce, actions can be prevented? but in what order does combat resolve? player-order favors player 1 ; maybe random order?
    - what if just make it sequential? use move-action economy like Zephon, or action points? not being able to move but once in a turn is an interesting feature, relative to civ's move-by-move minutiae. *i want to minimize turn complexity*. like go, make some actions, move some units, such that only moving units could possibly explode in complexity on a large map
    - so... action points, *command* points? so a too-large army cannot be directed? can be increased by a building?

Misc
- what if some things granted extra action points, but only for specific things?
    - points to build units
    - points to work tiles
    - points to expand tiles
    - points to build specific buildings?
    - points for diplomatic actions?
- buildings gain effect relative to tile size. such that:
    - produce minerals at mines proportional to tile size
    - provide adjacency bonus *potentially* as a proportion of size
    - so a building (or even specific effect) may or may not be proportional
    - so...
        farm:
            yield: :food
            x: 1 (base)
            p: 0.5 (proportional to size)
    - yield of {resource} = x + (p * size)
        or:
            yields:
                food:
                    x: 1
                    p: 0.5

Let's encode!
- Buildings:
    - Farm: produce food
    - Mine: produce minerals
    - Factory: produce goods, spawn units
    - Lab: produce research
    - Hab: adjacency bonus?
    - Base: command point
    - make stranger or more specific effects *wonders*, which are either global or factional
- Resources:
    - Food, supports building and unit upkeeps
    - Materials, pays building upkeeps
    - Goods, builds buildings and units, pays unit upkeeps
    - Knowledge: contributes to general research points
- Tiles:
    - Size: 0 - (3 + n, via ideotech)
    - n => Habitability
    - Building slots: size / 2
    - Working an undeveloped tile sets its size to two, from 0
    - {Ability: Devastate} reduces the size of a tile. {Ability: Seize} works the tile, but it must be expanded with an action to gain a building slot. (Modifiable with ideotech? Seize at higher sizes, because you can work the buildings?)
- Ideotech:
    - "overlap" is a meaningful measure between factions
    - tier 0: precludes nothing
        - settlers? no, industry 1
        - base? military 0
        - roads? industry 0
        - hab? (increases adjacent habitability?) ecology 0
        - medbay? heals units there and adjacent? contact 0
        - sensor? science 0
    - tier 1:
        industry:
        -
            - settler
            - improve mine with pollution
        ecology:
        -
            - recycler (food, materials, and sink pollution)
            - xenology (no fungus movement penalty (the penalty is movement ends when entering a fungus square, and you cannot work it.))
        -
            - hybrid forests
            - fungus habitability
        science:
        -
            - sensor
            - data center (adjacency bonus)
        contact:
        -
            - embassy (treaty bonus)
            - ???
        military:
        -
            - bunker (unit power adjacency bonus)
            - ops loadout
        -
            - +1 to "attack"
            - defense turret (building that acts as a stationary unit)
                - They call it defense, but, how much less an act of offense is it, when one only brings guns to enforce the offense after it has occurred?
            - artillery loadout
        -
            - +1 to "attack"
            - +1 to "artillery"
            - bases grant +1 command point
            - gain +1 action point

Strategies:
- Ecology: tall, synergy with fungus and weather
    - Experience from sinking pollution
- Industry: wide, better to work tiles than expand them
    - Experience from developing an unworked tile.
- Science: speed-running an endgame, use weird abilities as military and economic solutions
- Contact: victory through trade or diplomacy, and psychic stuff
- 
Axioms:
- Simplify rather than complect. Fewer mechanics, more comprehensibly, to minimally illuminate the thesis.
- Thesis of unthinkability: that to make a gun, the gun must be conceivable. in a society with no motive needing a gun, it does not come to exist.
- 