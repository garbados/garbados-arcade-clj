(ns planetcall-next.web
  (:require
   ["pixi.js" :refer [Application]]
   [planetcall-next.pixi.map :refer [create-map-view]]
   [planetcall-next.pixi.nav :refer [create-nav-view]]
   [planetcall-next.pixi.title :refer [create-title-menu]]
   [planetcall-next.rules.scenarios :as scenarios]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.pixi.utils :as pixi]))

(set! *warn-on-infer* false)

(defn start [app]
  (set! (-> app .-stage .-eventMode) "static")
  (set! (-> app .-stage .-hitArea) (.-screen app))
  (let [-game (atom (scenarios/init-game-from-scenario :standard))
        -views (atom {})
        activate-view
        (fn [state]
          (let [view (get @-views state)
                other-views (disj (set (keys @-views)) state)]
            (doseq [view-name other-views
                    :let [view (get @-views view-name)]]
              (set! (.-visible view) false))
            (set! (.-visible view) true)))
        title-view (create-title-menu :click-continue-game #(activate-view :map))
        map-view (create-map-view app -game)
        nav-view (create-nav-view
                  :on-click-game #(activate-view :game)
                  :on-click-map #(activate-view :map))]
    (let [offset [(-> app .-screen .-width)
                  (-> app .-screen .-height)]]
      (pixi/anchor-container offset title-view 0.5 1))
    (swap! -views assoc :game title-view)
    (swap! -views assoc :map map-view)
    (activate-view :game)
    (.stage.addChild app title-view map-view nav-view)))

(defonce -started? (atom false))

(defn init-app []
  (let [app (new Application)]
    (.then
     (.init app (clj->js {:background colors/BLACK
                          :resizeTo js/window}))
     (fn [] app))))

(defn main []
  (if-not @-started?
    (.then
     (init-app)
     (fn [app]
       (reset! -started? true)
       (start app)
       (js/document.body.appendChild (.-canvas app))))
    (js/location.reload)))

(main)
