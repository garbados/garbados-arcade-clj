(ns planetcall-next.web
  (:require
   ["pixi.js" :refer [Application Container Graphics]]
   ["@pixi/ui" :refer [Button]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.web.colors :as colors]))

(set! *warn-on-infer* false)

(defn new-game-button []
  (let [view (new Container)
        outer-view (new Container)
        text (pixi/->text "New Game" {:fontSize 30 :fill colors/WHITE})
        bg-w (.-width text) bg-h (.-height text)
        bg (-> (new Graphics)
               (.roundRect 0 0 bg-w bg-h 45)
               (.fill colors/BLACK))
        button (new Button view)]
    (.addChild view text bg)
    (.addChild outer-view button)
    (set! (.-enabled button) true)
    (.onHover.connect button (fn []
                               (set! (.-style.-fill text) colors/BLACK)
                               (set! (.-style.-fill bg) colors/WHITE)))
    (.onOut.connect button (fn []
                             (set! (.-style.-fill text) colors/WHITE)
                             (set! (.-style.-fill bg) colors/BLACK)))
    outer-view))

(defn create-title-menu []
  (let [title-text (pixi/->text "- P L A N E T C A L L -" {:fontSize 70 :fill colors/WHITE})
        continue-game-text (pixi/->text "Continue" {:fontSize 30 :fill colors/WHITE})
        new-game-text (new-game-button)
        load-game-text (pixi/->text "Load Game" {:fontSize 30 :fill colors/WHITE})
        settings-text (pixi/->text "Settings" {:fontSize 20 :fill colors/WHITE})
        credits-text (pixi/->text "Credits" {:fontSize 20 :fill colors/WHITE})
        title-menu (new Container)
        title-menu-texts (new Container)
        w-margin 30
        h-margin 30
        text-objects [title-text continue-game-text new-game-text load-game-text settings-text credits-text]]
    (pixi/move-to title-menu-texts [w-margin h-margin])
    (pixi/move-below title-text continue-game-text 30)
    (pixi/move-below continue-game-text new-game-text)
    (pixi/move-below new-game-text load-game-text)
    (pixi/move-below load-game-text settings-text 30)
    (pixi/move-below settings-text credits-text)
    (doseq [obj text-objects]
      (.addChild title-menu-texts obj)
      (.anchor.set obj 0.5 0))
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

(defn init [app]
  (set! (-> app .-stage .-eventMode) "static")
  (set! (-> app .-stage .-hitArea) (.-screen app))
  (let [title-menu (create-title-menu)
        [ox oy] [(-> app .-screen .-width)
                 (-> app .-screen .-height)]]
    (pixi/anchor-container [ox oy] title-menu 0.5 1)
    (.stage.addChild app title-menu)))

(defn main []
  (let [app (new Application)]
    (.then
     (.init app (clj->js {:background colors/BLACK :resizeTo js/window}))
     (fn []
       (init app)
       (js/document.body.appendChild (.-canvas app))))))

(main)
