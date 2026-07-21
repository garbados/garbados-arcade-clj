(ns scripts.build-html
  (:require
   [clojure.edn :as edn]
   [hbs.core :as hbs]))

;; no ns
(def arcade-config (edn/read-string (slurp "config.edn")))
(def index-template (slurp "resources/arcade/index.hbs"))
(def game-template (slurp "resources/arcade/game.hbs"))

(spit "public/index.html" (hbs/render index-template arcade-config))
(doseq [game-config (:games arcade-config)]
  (spit (str "public/" (:name game-config) ".html")
        (hbs/render game-template game-config)))
