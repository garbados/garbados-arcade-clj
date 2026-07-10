(ns ambition.web.views
  (:require
   ["pixi.js" :refer [Container Graphics]]
   [ambition.text :as text]
   [arcade.pixi.colors :as colors]
   [arcade.pixi.utils :as pixi]))

;; TODO can-continue?

(defn create-title-view
  [& {:keys [click-continue-game
             click-new-game
             click-settings
             click-credits]}]
  (let [title-text         (pixi/->text "A M B I T I O N" {:fontSize 70 :fill colors/WHITE})
        subtitle-text      (pixi/->text text/intro {:fontSize 35 :fill colors/DIM-GRAY :align "center"})
        continue-game-text (pixi/create-button "Continue" :on-click click-continue-game)
        new-game-text      (pixi/create-button "New Game" :on-click click-new-game)
        settings-text      (pixi/create-button "Settings" :on-click click-settings)
        credits-text       (pixi/create-button "Credits" :on-click click-credits)
        title-menu         (new Container)
        title-menu-texts   (new Container)
        w-margin 30 h-margin 30
        buttons [continue-game-text new-game-text settings-text credits-text]]
    (pixi/move-to title-menu-texts [w-margin h-margin])
    (pixi/move-below title-text subtitle-text 5)
    (pixi/move-below subtitle-text continue-game-text 5)
    (pixi/move-below continue-game-text new-game-text 5)
    (pixi/move-below new-game-text settings-text 5)
    (pixi/move-below settings-text credits-text 5)
    (doseq [obj (concat [title-text subtitle-text] buttons)]
      (.addChild title-menu-texts obj))
    (.anchor.set title-text 0.5 0)
    (.anchor.set subtitle-text 0.5 0)
    (doseq [button buttons]
      (pixi/anchor-container [(.-x button)
                              (+ (.-y button) (.-height button))]
                             button
                             0.5 1))
    (let [{:keys [w h]} (pixi/container-size title-menu-texts)
          bg-w (+ (* 2 w-margin) w)
          bg-h (+ (* 2 h-margin) h)
          title-menu-bg (-> (new Graphics)
                            (.roundRect 0 0 bg-w bg-h 45)
                            (.fill colors/BLACK))]
      (set! (.-pivot.-x title-menu) (- (.-x title-menu) (/ w 2)))
      (.addChild title-menu
                 title-menu-bg
                 title-menu-texts))
    title-menu))

