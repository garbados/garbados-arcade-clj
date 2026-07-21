# The Island

> "*The Stanley Parable* according to Diana." - Luci

*The Island* is a 2D adventure game where you play as a stranger to an isolated island community, answering the local shaman's summons for your father. Gameplay is heavily inspired by Skyrim, Wildermyth, and Majora's Mask.

Arriving as a young adult, you have few aptitudes to your name. The shaman implores you to quest to the inside of the mountain at the island's center, but it will be a long, dangerous journey. You travel through the island's four villages on your way, making friends and maybe even enemies, growing skills and obtaining equipment. By the time you enter the heart of the island, will you be prepared for what you find?

## Features Summary

- 2D world exploration with instanced areas.
- Realtime tempo-centric combat. Dodge on time and hit to the flow!
- Reputation-based commerce. The Island's only currency is trust.
- Numerous skills, powerful talents, detailed equipment.
- Learn by doing. Gain experience from virtually anything.
- Limited survival mechanics, including hunger, sleep, and afflictions.
- Involved questlines, chittering NPCs, bustling villages.
- Calendar progress!
    - In-world events trigger at certain times, such as after a number of days since game-start.
    - Events may trigger differently depending on what prerequisites have been met.
    - In-game activities consume calendar time as you spend hours and days in concerted efforts.
- New Game Plus!
    - Complete the main questline to start another run with all your skills!
- Built-in mod system!
    - Subsequent runs can be expanded with "reveries" which add content to the world.
    - Reveries are just gzipped packs of data files, which contain assets and object definitions.
    - Definitions use a declarative syntax rather than a Turing-complete language.
    - Add mod-hubs by URL. Run them yourself for fun and profit!

## Mechanics

These descriptions apply to core gameplay:

- The player has an avatar with many qualities:
    - Skills: proficiencies that improve abilities, unlock dialogue, talents, etc.
        - Melee: Effect of melee attacks; affects damage, critical effect.
        - Ranged: Effect of ranged attacks; affects damage, reload speed, etc.
        - Arcana: Effect of spells and magical items; affects enchanting.
        - Bulwark: Improves endurance, effect of armor, etc.
        - Sneak: Reduces zone of detection, increases sneak attack damage, improves pickpocketing.
        - Cooking: Contributes to Cooking activities.
        - Alchemy: Contributes to Alchemy activities.
        - Smithing: Contributes to Smithing activities.
        - Diplomacy: Multiplies effect of reputation.
        - Tar-iki: Unlocks dialogue with more locals.
    - Talents: gain one each time you level up. Associated with skills; unlock more with higher skill.
        - Abilities: new attacks, spells, etc.; improve known attacks, critical effects, etc.
        - Activities: new activities, unlocks ability to decipher new classes of blueprints.
        - Passives: invest in growing particular existing qualities.
    - Combat:
        - Realtime combat with implicit/intentional targeting of hostiles and friendlies.
        - As if on a gamepad:
            - Use left stick to move the avatar.
            - A: dash. X: attack. Y, B: can be set from known abilities.
            - Use shoulder buttons to cycle Y from favorites. Use triggers to cycle B from favorites.
            - Use right stick to "point" to target an NPC. You can only harm friendly NPCs if you target them explicitly, by clicking the right stick while pointing. Pointing will target hostiles automatically. Pointing again will select a different NPC in that direction (in case of several matches).
        - Weapon type supplies an Attack ability with a critical trigger-timer, which causes a special effect when satisfied. (Ex: spear can activate a lunge right after a dash, sword can parry just before a hit would land, etc.)
        - Combat ends when no hostiles are aware of you.
        - Sneak up on NPCs to grant sneak damage on your first attack.

## Architecture

The game's engine implements how to interpret provided data files. The game's core campaign is implemented as bundled data files. Data files *define objects* which can be of many types and contain many details. Types include:

- Skill, including icon, ID, name, description, etc.
- Talent, including icon, ID, name, description, effect, etc.
- Ability, including animations, ID, description, effect, etc.
- NPC, including sprite, animations, name, location, relationships, interactions, etc.
- Quest, involving requirements, triggers, stages, a cast of NPCs, quest objects, etc.
- Equipment, involving assets, stats, conditions where it may appear, etc.
- Activity, including requirements, duration, practiced skills or threshold expertise, etc.
- Dialogue, where one or more NPCs engage the player's avatar in conversation.
- Scene, where one or more NPCs act something out autonomously.
- Item, involving icon, an effect if any, tags, etc.
- Location, including ID, name, layout data, assets, etc.
- Image / Animation, including file paths and how to parse them.

Definitions are linked together by naming IDs. ID conflicts can be determined at startup, as data files are validated before interpretation. So, an NPC may specify the animations associated with it by ID, where that ID is specified by the Animation definition associated with it. The animation itself is a file blob that is interpreted according to the associated definition. Different object definition types share fields, such as tags, names, descriptions, and such.

Data files may be included at game instantiation in some load order. They can be provided as references, such as internal names like "_main", but also as URLs which resolve to a gzipped archive of data files and blobs. Mod hubs provide other facilities like checking mod versions, to know whether a URL can load from cache or will require an update. Mod hubs can also be queried for the mods they contain, which populates the in-game interface for adding mods to game runs. *Mods can only be added or removed before a given run, so, before it starts, or after completing the main quest before starting the next run.*

Mod hubs are web servers that manage user authentication and allow uploading of mods as gzipped archives. These archives can be validated and then indexed. They may require authentication to use, but default to public access.

*The intent of the declarative syntax exposed to data files* is to provide a high-level API for easily coding involved thematic contributions to the game world. By tailoring this language to a 2D environment, we can more easily implement high-quality interactive possibilities, rather than struggling to balance clever efficiencies with modding experience within a comprehensive 3D conceptualization. *I want it to be as simple as HTML to make your own game like this.*

## Lore

**This information contains spoilers.**

- You hail from a medieval archipeligan society called the Blue Sky Reign, a metropolitan tribal confederation dotted with centers of industry and learning. (What if this part is configurable?)
- Your father, Ernuq'wa, grew up on a distant spit called *At'tarish*, which means "the island" in the local tongue. You learned a few words of that language, *tar-iki*, but you have never spoken it with anyone. Several years ago, he perished in his sleep after a lifetime as a local artisan. (What if this part is configurable, and grants skills?)
- A messenger delivers a summons to you, addressed to your father, from the chief shaman of At'tarish. He says he has uncovered the final pieces of the mystery that vexed the two of them as children, but requires one of able body to "delve the heart for its sacred mystery."
- You charter passage on a merchant vessel headed for At'tarish, and make some conversation with the crew. (Dialogue supplies skills?)
- Once you arrive, you make land in the port village of Ni-irik, where the shaman awaits you on the beach. He leads you back to his hut, where he sends his acolytes away to speak with you. He calls you Ernuq'wa, but you can give him a different name to call you. (Sets player name.) He charges you with venturing to the center of the mountain at the center of the island, and gives you a glowing stone that he says "is the key."
- If you try to leave by speaking with the merchant vessel's crew, they tell you that they won't disembark for several weeks. The crew is taking shore leave while the villages coordinate supply agreements with the vessel. "Time moves differently on At'tarish," they joke, "But guest-right is tradition here. You can get what you need from village commissaries, but don't expect more than that for free."
- *Then the main game loop opens up. Go anywhere. Do anything. The clock is ticking.*
- ...
- When the player's avatar enters the heart of the mountain, they find a temple with great stone doors that open when presented with the glowing stone. Inside, indecipherable documents detail the functioning of fanciful machinery, whose remains rot around these forgotten rooms. Glass tubes expose veins of lava weaving through the walls. A pulsing obelisk at one end draws your attention, which seems to be being fed by countless pipes and cables. It takes hold of you, and the island begins to shake apart. You receive a vision of a young man in the same room, who seems to be torn apart and sucked away by these mysterious forces. When it lets go of you, the boy's ghost says, "Oh no. What has happened to the island?" and vanishes rushing to the temple exit. You return to open air directly outside the temple, as the volcanic explosion has blown the top off the mountain. Pieces of its summit, flung around the landscape, scar it. Villages lay in fire and ruin. Dodging lava veins and spirit-creatures, you shepherd lingering NPCs to the shore where the merchant vessel prepares to disembark. As you step onto the boat yourself, your vision goes white. Then At'tarish the God appears.
- At'tarish the God reveals that you were created as its avatar on the island, to bring a kinder fate to the people than that merciless destruction. He invites you to return to the island to try again, keeping all that you'd learned the first time around. This brings you back to the beach of Ni-irik, where the local shaman reveals the quest to you that you had just concluded.
- The game loop continues...
- If the player does not complete the main questline within thirty days, the volcano erupts, ending the current run.
- *If the player kills all NPCs on the island,* At'tarish seizes the player's avatar and demands to know why they would visit such senseless violence on undeserving people. The avatar, on its own, asserts that they aren't people at all but nostalgic fantasies. Whatever really existed of those people died a very long time ago, and this cyclic memorial to their memories does not honor them with genuine grief, but makes self-amusing golems of their remains. At'tarish flies into a rage and manifests into a shadow of you, which tries to kill you.
- Whether you defeat At'tarish or not, the encounter ends the current run, forcing you to start all over again.
