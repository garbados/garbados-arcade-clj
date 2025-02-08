(ns planetcall-next.web
  (:require
   ["phaser" :as Phaser]
   ["phaser3-rex-plugins/plugins/board-plugin.js" :as BoardPlugin]
   [planetcall-next.web.config :as config]
   [planetcall-next.web.scenes.map :refer [create-map-scene]]
   [planetcall-next.web.scenes.ui :refer [create-ui-scene]]
   [shadow.cljs.modern :refer [defclass]]))

(set! *warn-on-infer* false)

(defn create-title-screen [scene]
  nil)

(defn create-tech-scene [scene]
  nil)

(defn create-wiki-scene [scene]
  nil)

(defclass TitleScreen
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "title"})))
  Object
  (create [this] (create-title-screen this)))

(defclass UIScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "ui" :active true})))
  Object
  (create [this] (create-ui-scene this :active :tech)))

(defclass MapScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "map"})))
  Object
  (create [this] (create-map-scene this)))

(defclass TechScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "tech"})))
  Object
  (create [this] (create-tech-scene this)))

(defclass WikiScene
  (extends js/Phaser.Scene)
  (constructor [this] (super (clj->js {:key "wiki"})))
  Object
  (create [this] (create-wiki-scene this)))

(def game-config
  (clj->js {:type (.-AUTO Phaser)
            :scene [TitleScreen MapScene TechScene WikiScene UIScene]
            :width config/WIDTH
            :height config/HEIGHT
            :parent "app"
            :scale {:mode (-> Phaser .-Scale .-FIT)
                    :autoCenter (-> Phaser .-Scale .-CENTER_BOTH)}
            :plugins {:scene [{:key "rexBoard"
                               :plugin BoardPlugin/default
                               :mapping "rexBoard"}]}}))

(defonce game (atom nil))

(defn main []
  (if @game
    (js/window.location.reload)
    (reset! game (new (.-Game Phaser) game-config))))

(main)
