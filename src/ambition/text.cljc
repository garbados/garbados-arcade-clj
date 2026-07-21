(ns ambition.text
  (:require
    [arcade.macros :refer-macros [inline-slurp]]))

(def intro (inline-slurp "resources/ambition/intro.txt"))
(def awards
  {:death-cult   (inline-slurp "resources/ambition/death_cult.txt")
   :highlander   (inline-slurp "resources/ambition/highlander.txt")
   :malthus      (inline-slurp "resources/ambition/malthus.txt")
   :sacrifice    (inline-slurp "resources/ambition/sacrifice.txt")
   :humanitarian (inline-slurp "resources/ambition/humanitarian.txt")})
(def gameover (inline-slurp "resources/ambition/gameover.txt"))
