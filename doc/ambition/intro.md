# ambition-game

A game about who you are together when eras end.

```
> THE WORLD IS ENDING.
> THE SKY CHURNS WITH POISON AND TOXIC SEAS BOIL.
> YOUR PEOPLE MUST SURVIVE.
> WILL THEY?
```

This project contains the digital board-game *Ambition*. This file explains how
to run and play the game. Up to four players can play together on a single
computer, via hotseat (classic).

## Rules

On an 6x6 square board, two to four players compete. Each player begins with two
*squadrons* on two spaces with 1 *infrastructure*. Here are those spaces for
each player, keyed by their number (first player: 1, second player: 2, etc.):

```
- - 1 1 - -
- - - - - -
3 - - - - 4
3 - - - - 4
- - - - - -
- - 2 2 - -
```

One by one they choose actions -- first player first, second player second, etc.
-- until all players have acted. When all players have taken their turn, the
round ends and a new round begins.

At the end of each round one random passable, unclaimed space is *despoiled*,
making it impassable. If there is a unit on this space, it is shunted to a
random adjacent space. If all adjacent spaces are occupied or impassable, the
unit is destroyed.

When there are no passable, unclaimed spaces left on the board, the game ends.
Whoever has the most *infrastructure* wins. Players with more than 10
*infrastructure* survive the season of destruction that follows the game.

On a player's turn, they must choose one of the following actions:

- **Develop**: Raise the *infrastructure* of a space by 1, to a maximum of 2.
- **Despoil**: Lower the *infrastructure* of a space to 0. If it is already 0,
the space becomes *despoiled*, making it impassable for the rest of the game.
- **Muster**: Spawn a unit on a space you control. If its *infrastructure* is 1,
spawn a *squadron*. If it is 2, spawn a *battalion*.
- **Advance**: Move a unit. If it moves onto the space of another unit that is
*flanked*, that unit is destroyed.

## Terminology

- *Despoiled*: A despoiled space is impassable. Units cannot move onto it, but
*battalions* can jump over it. *Despoiled* spaces are so toxic that they cannot
be restored. Any space not despoiled is *passable*.
- *Infrastructure*: A space can have 0, 1, or 2 *infrastructure*. It is a
measure of the development of an area, including population centers as well as
military assets. More developed areas can field more powerful units. Communities
require at least 10 *infrastructure* to maintain the equipment and processes
needed to survive the climax of the apocalypse.
- *Claimed / Unclaimed*: A space is *unclaimed* while it has 0 *infrastructure*.
Once it has more than 0 *infrastructure*, it is *claimed* by the last player to
have a unit on that space. Thus, a *claimed* space can be seized by moving a
unit onto it.
- *Flanked*: A unit is *flanked* if more than one enemy unit could move onto its
space. For example, a unit adjacent to at least two *squadrons* controlled by
other players is *flanked* because both *squadrons* could move onto it.
- *Squadron*: A unit that can move one space on an **advance** action.
- *Battalion*: A unit that can move two spaces on an **advance** action. This
means it can *flank* at a range of two spaces.
