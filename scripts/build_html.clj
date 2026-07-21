(ns scripts.build-html
  (:require
   [clojure.edn :as edn]
   [hbs.core :as hbs]))

(def dev-status
  {:development "in development"
   :alpha "playable alpha"
   :beta "beta testing"
   :done nil})

(def css-themes
  {:bulma "bulma.min.css"})

(defn map-update [x key fn]
  (update x key #(map fn %)))

(def arcade-config
  (-> (edn/read-string (slurp "config.edn"))
      (update :css css-themes)
      (map-update :games #(-> %
                              (update :css css-themes)
                              (update :status dev-status)))))
(def index-template (slurp "resources/arcade/index.hbs"))
(def game-template (slurp "resources/arcade/game.hbs"))

(spit "public/index.html" (hbs/render index-template arcade-config))
(doseq [game-config (:games arcade-config)
        :let [game-config
              (-> game-config
                  (update :css css-themes)
                  (update :status dev-status))]]
  (spit (str "public/" (:name game-config) ".html")
        (hbs/render game-template game-config)))
