(ns dimdark.games 
  (:require [clojure.spec.alpha :as s]))

(s/def ::game
  (s/keys :req-un [::kobolds
                   ::equipment
                   ::items
                   ::adventure
                   ::escapade]))
