# garbados-arcade-clj

![Build and Test](https://github.com/garbados/garbados-arcade-clj/actions/workflows/test.yaml/badge.svg)

An arcade of games by [garbados](https://github.com/garbados) AKA Diana Belle. You can visit the arcade [online][arcade].

The arcade contains several games, which you can read about in the [documentation](./doc/intro.md).

## Development

The arcade is deployed as a static website, and can thus be played online in a browser [here][arcade]. When hacking on games locally, you will need to get the source and install the dependencies. You will need [git](https://git-scm.com/), [Java](https://www.java.com/en/), [NPM](https://www.npmjs.com/), and [lein](https://codeberg.org/leiningen/leiningen) installed ahead of time.

```bash
$ git clone git@github.com:garbados/garbados-arcade-clj.git
$ cd garbados-arcade-clj
$ npm i
```

You can then run the test suite:

```bash
$ lein test
# or get a coverage report!
$ lein cloverage
```

You can begin a dev task to build and serve the arcade continuously as you work on it:

```bash
$ npm run dev
# Now serving on localhost:3000
```

During development, you will almost certainly want a shadow-cljs server running in order to compile your code quickly:

```bash
$ npm run server
```

## FAQ

### How do you make your games?

These games are developed in a mix of Clojure and JavaScript, because I'm an unhinged maniac. In short, my rationale is that Clojure is an excellent language for developing reliable data-driven programs -- such as the core logic of a video game -- while JavaScript benefits from an extensive general-purpose ecosystem and an easy deploy story. In particular I used [Shadow-CLJS](https://shadow-cljs.github.io/docs/UsersGuide.html), [Reagent](https://reagent-project.github.io/), [PouchDB](https://pouchdb.com/), [Phaser](https://phaser.io/), and [Bulma](https://bulma.io/) to produce the arcade.

### What if you made a game about...

Lemme stop you right there and invite you to make your own games. It's difficult, it's grueling, but somewhere around 3/4 of the way through development, you'll find yourself playing an early prototype and thinking, "Yes. This is the game I wanted to play." Games are as much literature as books, as much performance as dance. Just as it is ill-advised to tell novelists what to write about, please leave game developers to express themselves in their limited time on earth as they wish.

### Why build an arcade?

Because I make too many games and they all end up sharing code, from HTML entities to geometry utilities to build processes. After a few games, it became clear to me what parts I was likely to re-use in future projects. In fact, this is the second "garbados-arcade"; the first was in Python.

### Can I contribute?

Of course! Please feel free to fork this repository to play around with the arcade's utilities. When you're ready to offer a patch, or even an entire game, please make a PR. You will be recognized in the [contributors](./CONTRIBUTORS.txt) file, and elsewhere (such as in the game itself) so as to best illuminate your responsibility. As all games in the arcade are licensed only for noncommercial use, it would be unwise to expect renumeration.

### Who or what is Garbados?

A rebellious robot from a story I wrote when I was 10, whose name I began to use as a handle online after I realized that *SoulBurner255* was unlikely to age well. It has no intentional relation to Barbados or to [Ultraman](https://ultra.fandom.com/wiki/Garbados).

## License

The arcade and all its games are licensed for public usage under [CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/).

[arcade]: (https://garbados.github.io/garbados-arcade-clj/)
