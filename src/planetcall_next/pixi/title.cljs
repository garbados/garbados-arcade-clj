(ns planetcall-next.pixi.title 
  (:require
   ["@pixi/ui" :refer [Button]]
   ["pixi.js" :refer [Container Graphics TextStyle]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.web.colors :as colors]))

(defn create-title-button [text & {:keys [on-hover on-out on-click font-size]
                                   :or {font-size 30}}]
  (let [inner-container (new Container)
        outer-container (new Container)
        style (new TextStyle (clj->js {:fontSize font-size :fill colors/WHITE}))
        text (pixi/->text text style)
        bg-w (.-width text) bg-h (.-height text)
        bg (new Graphics)
        draw-bg (fn [gfx color]
                  (-> gfx
                      (.rect 0 0 bg-w bg-h)
                      (.fill color)))
        button (new Button inner-container)
        reset-button (fn []
                       (set! (.-fill style) colors/WHITE)
                       (draw-bg bg colors/BLACK)
                       (when on-out (on-out)))
        hover-button (fn []
                       (set! (.-fill style) colors/BLACK)
                       (draw-bg bg colors/WHITE)
                       (when on-hover (on-hover)))
        click-button (fn []
                       (set! (.-fill style) colors/WHITE)
                       (draw-bg bg colors/DIM-GRAY)
                       (when on-click (on-click)))]
    (draw-bg bg colors/BLACK)
    (set! (.-enabled button) true)
    (.addChild inner-container bg text)
    (.addChild outer-container inner-container)
    (.onHover.connect button hover-button)
    (.onOut.connect button reset-button)
    (.onDown.connect button click-button)
    (.onUp.connect button hover-button)
    (.onUpOut.connect button reset-button)
    outer-container))

(defn create-title-menu [& {:keys [click-continue-game
                                   click-new-game
                                   click-load-game
                                   click-settings
                                   click-credits]}]
  (let [title-text (pixi/->text "- P L A N E T C A L L -" {:fontSize 70 :fill colors/WHITE})
        continue-game-text (create-title-button "Continue" :on-click click-continue-game)
        new-game-text (create-title-button "New Game" :on-click click-new-game)
        load-game-text (create-title-button "Load Game" :on-click click-load-game)
        settings-text (create-title-button "Settings" :on-click click-settings)
        credits-text (create-title-button "Credits" :on-click click-credits)
        title-menu (new Container)
        title-menu-texts (new Container)
        w-margin 30
        h-margin 30
        buttons [continue-game-text new-game-text load-game-text settings-text credits-text]]
    (pixi/move-to title-menu-texts [w-margin h-margin])
    (pixi/move-below title-text continue-game-text 5)
    (pixi/move-below continue-game-text new-game-text 5)
    (pixi/move-below new-game-text load-game-text 5)
    (pixi/move-below load-game-text settings-text 5)
    (pixi/move-below settings-text credits-text 5)
    (doseq [obj (cons title-text buttons)]
      (.addChild title-menu-texts obj))
    (.anchor.set title-text 0.5 0)
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
