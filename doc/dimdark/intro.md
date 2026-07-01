# Introduction to *The Dimdark*

> Six devoted kobolds serve a dying dragon. She knows she is dying, and there is nothing to do for it. The warren of the kobolds dwindled over the years until at last the dragon advised its inhabitants to seek other lands. Now she only awaits death as a toxic crystal embedded deep in her body drains her life away. The loyal six defied her counsel out of love, so she decided to put them to use. She bids them to delve the dimdark below for the emblems and trinkets of her past -- things that remind her of her life, her loves, and the brighter days of her spirit's dimming light. Some of these things are thousands of years old, but she knows exactly where they are, and she sends her faithful to obtain them. So, they do.

*The Dimdark* is a turn-based party battler, where opposing groups form ranks and fight until the last standing remains. Inbetween narrative excursions, four chosen kobolds will combine their abilities to overcome mortal danger with martial prowess. As they delve and adventure, they will find loot, level up, and uncover the secrets of what lies below.

This edition of the game follows a prior edition written in Python as a terminal game. This update modifies many core game systems, owing to lengthy reflection on what was and wasn't working. Not to mention, it translates the game into Clojure.

## Kobolds

Each kobold has a *class* that indicate the *skills* they will learn as they level up. Each kobold is limited to four *activated* skills, knowing three to begin with, and gaining access to three more with each levelup, for a total of 15 skills per class. Some skills are *active*, needing to be used to take effect, while others are *passive* and have an effect at all times; both use a skill slot. You can read more about classes under [Classes](./classes.md), and about skills in [Skills](./skills.md).

The kobolds, in short:

- Drg: the MAGE, reckless and fierce, eager to seize any advantage.
- Grp: the DRUID of the buried green, healer and ally of what creeps and crawls.
- Knz: the RANGER of the mushroom forests, keen of eye and toxin alike.
- Muu: the WARRIOR come home; worldly, trained in exotic techniques with strange weapons.
- Yap: the SNEAK, fleeing the authorities of upworld countrysides; a dabbling arcanist and deft shadow.
- Yip: the GUARDIAN, clad in plate mail, as stalwart as their own shield.

Kobolds have *equipment*, which you can read about in [Equipment](./equipment.md). Kobolds, like monsters, are a type of creature. You can read more about creature basics in [Creatures](./creatures.md), and about monsters in [Monsters](./monsters.md).

When a kobold levels up, you will have the opportunity to assign some attribute gains, in addition to the gains the kobold achieved automatically.

## Your Dragon

Your dragon, whose many names are lost to time, is dying. With each encounter the party faces, her end draws a nearer. Each encounter grants one experience to the party, and they level up every 20 levels, to a maximum of level 5 at 100 experience. Time works differently in the lightless underground; it is not days or weeks that experience represents, but a span that yawns indeterminate like the ravines of the hungry dark. That said, the party levels up on the first day of the every new year; a sort of collective birthday and calendar festival. You don't know when your dragon will die, but you do know the day approaches, looming inevitably.

## Quests and Delves

Your patron dragon will task you to obtain relics, to go on *quests* to do so. There are only a certain number of quests in the game, and they are themed to include only certain monsters. They conclude with a boss fight. If the kobolds survive all of that, they obtain the relic and return it to their dragon.

Kobolds can also go on *delves*, where they venture into the dimdark for loot and experience. They will meet whatever assortment of creatures the pitch void has driven mad, one fight after another, but the rewards and dangers steadily grow. Once you have reached five levels, and for each five levels after, you will be able to return to that difficulty on subsequent delves, rather than having to fight all the way back.

Quests and delves are both *adventures*.

## Combat

When kobolds on an adventure meet with hostility, they enter an *encounter*. They form into front and rear lines, as their enemies do. During each *round* of the encounter, each creature takes a *turn* in order of *initiative*. During their turn, a creature may use a single skill. Once all creatures have gone, the round ends and a new one begins. This continues until only one party remains.

As skills deal damage, creatures will *collapse*, lowering the curtain over death itself. A collapsed monster is removed from combat, but you may revive collapsed kobolds with healing magic and items.

The front line of a party can use *close* skills against the opposing front line, such as melee attacks. The rear line can use *distant* skills, such as ranged attacks and spells, and are harder to hit with ranged attacks.

If there are no creatures in the front line, or all such creatures have collapsed, then the party *loses ground* and the creatures on the back row are pushed into the front as the opposing party closes the distance.

After each encounter, if the party survived, they heal to full and shrug off any statuses. If they didn't, their comrades drag them back to the warren where their dragon restores them to health. However, the party loses any loot it had found.

## Acknowledgements

Thanks go to the likes of *Super Mario RPG*, *Final Fantasy IX*, *Diablo II*, *Path of Exile*, and *Wildermyth* for inspiring me to make this game. I wanted to fuse the menu-driven class-oriented party battler of classical JRPGs with the equipment dynamics of Diablo-likes, though I smoothed the acquisition curve because playing lottery isn't fun. I wrote it as a love letter for my wife and the time we have together in life.

## Etc.

See [Notes](./notes.md).
