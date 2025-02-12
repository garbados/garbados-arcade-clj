(ns planetcall-next.web
  (:require
   ["pixi.js" :refer [Application]]
   [planetcall-next.pixi.map :refer [create-map-view]]
   #_[planetcall-next.pixi.title :refer [create-title-menu]]
   [planetcall-next.pixi.utils :as pixi]
   [planetcall-next.web.colors :as colors]))

(set! *warn-on-infer* false)

(defn init [app]
  (set! (-> app .-stage .-eventMode) "static")
  (set! (-> app .-stage .-hitArea) (.-screen app))
  (let [radius 32
        n 6
        map-view (->> (create-map-view radius n)
                      (pixi/scrollable-container app)
                      (pixi/zoomable-container))
        offset [(-> app .-screen .-width (- radius) (/ 2))
                (-> app .-screen .-height (- radius) (/ 2))]]
    (.scale.set map-view 2)
    (pixi/move-to map-view offset)
    (.stage.addChild app map-view)))

(defn main []
  (let [app (new Application)]
    (.then
     (.init app (clj->js {:background colors/BLACK :resizeTo js/window}))
     (fn []
       (init app)
       (js/document.body.appendChild (.-canvas app))))))

(main)
