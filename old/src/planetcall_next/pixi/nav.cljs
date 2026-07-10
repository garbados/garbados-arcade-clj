(ns planetcall-next.pixi.nav 
  (:require
   ["pixi.js" :refer [Container]]
   [planetcall-next.pixi.utils :as pixi]))

(defn create-nav-view
  [& {:keys [on-click-game on-click-map on-click-tech on-click-wiki]}]
  (let [container (new Container)
        game-button (pixi/create-button "Game" :border-width 2 :on-click on-click-game)
        map-button (pixi/create-button "Map" :border-width 2 :on-click on-click-map)
        tech-button (pixi/create-button "Tech" :border-width 2 :on-click on-click-tech)
        wiki-button (pixi/create-button "Wiki" :border-width 2 :on-click on-click-wiki)]
    (pixi/move-right game-button map-button)
    (pixi/move-right map-button tech-button)
    (pixi/move-right tech-button wiki-button)
    (.addChild container game-button map-button tech-button wiki-button)
    container))