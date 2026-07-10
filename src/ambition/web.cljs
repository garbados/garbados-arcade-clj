(ns ambition.web
  (:require
   [ambition.web.views :as views]
   [arcade.pixi.utils :refer [start-app!]]
   [arcade.pixi.utils :as pixi]))

(defn continue-game! []
  'todo)

(defn new-game! []
  'todo)

(defn show-settings! []
  'todo)

(defn show-credits! []
  'todo)

(defn boot-game! [app]
  (set! (-> app .-stage .-eventMode) "static")
  (set! (-> app .-stage .-hitArea) (.-screen app))
  (let [-views (atom {})
        -view (atom nil)
        title-view (views/create-title-view
                    :click-continue-game continue-game!
                    :click-new-game      new-game!
                    :click-settings      show-settings!
                    :click-credits       show-credits!)
        offset [(-> app .-screen .-width)
                (-> app .-screen .-height)]]
    (pixi/anchor-container offset title-view 0.5 0)
    (swap! -views assoc :title title-view)
    (reset! -view :title)
    (.stage.addChild ^js/Object app title-view)))

(defonce -started? (atom false))

(start-app! -started? boot-game!)
