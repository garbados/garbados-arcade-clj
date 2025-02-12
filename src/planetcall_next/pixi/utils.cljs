(ns planetcall-next.pixi.utils
  (:require ["pixi.js" :refer [Text Container]]))

(set! *warn-on-infer* false)

(defn ->text
  ([s style & {:as options :or {options {}}}]
   (new Text (clj->js (merge {:text s :style style} options)))))

(defn move-to [thing [x y]]
  (when x
    (set! (.-x thing) x))
  (when y
    (set! (.-y thing) y)))

(defn pivot-to [thing [x y]]
  (set! (.-pivot.-x thing) x)
  (set! (.-pivot.-y thing) y))

(defn move-below [above below & {:as padding :or {padding 0}}]
  (let [y (+ (.-y above) (.-height above) padding)]
    (set! (.-y below) y)))

(defn container-size [container]
  (reduce
   (fn [{:keys [x y w h]} child]
     {:x (min x (.-x child))
      :y (min y (.-y child))
      :w (js/Math.round (max w (+ (.-width child) (.-x child))))
      :h (js/Math.round (max h (+ (.-height child) (.-y child))))})
   {:x 0 :y 0 :w 0 :h 0}
   (.-children container)))

(defn anchor-container [[ox oy] container ax & [ay]]
  (let [{:keys [w h]} (container-size container)]
    (set! (.-x container) (-> ox (- w) (* ax)))
    (set! (.-y container) (-> oy (- h) (* (or ay ax))))))

(defn on-drag-handlers [app container & {:keys [horz vert]}]
  (let [on-drag-move
        (fn [event]
          (let [dx (-> event .-movement .-x)
                dy (-> event .-movement .-y)]
            (when horz
              (set! (.-x container) (+ (.-x container) dx)))
            (when vert
              (set! (.-y container) (+ (.-y container) dy)))))
        on-drag-start
        (fn []
          (when (.-visible container)
            (.stage.on app "pointermove" on-drag-move app)))
        on-drag-end
        (fn []
          (.stage.off app "pointermove" on-drag-move app))]
    {:move on-drag-move
     :start on-drag-start
     :end on-drag-end}))

(defn scrollable-container
  "Container that scrolls when anywhere on the screen is dragged."
  [app container
   & {:keys [horz vert] :or {horz true vert true}}]
  (let [outer (new Container)
        {on-drag-start :start
         on-drag-end :end} (on-drag-handlers app outer :horz horz :vert vert)]
    (.addChild outer container)
    (.stage.on app "pointerdown" on-drag-start app)
    (.stage.on app "pointerup" on-drag-end app)
    (.stage.on app "pointerupoutside" on-drag-end app)
    outer))

(defn draggable-container
  "Container that scrolls when it is dragged."
  [app container
   & {:keys [horz vert] :or {horz true vert true}}]
  (let [outer (new Container)
        {on-drag-start :start
         on-drag-end :end} (on-drag-handlers app outer :horz horz :vert vert)]
    (.addChild outer container)
    (set! (.-eventMode outer) "static")
    (.on outer "pointerdown" on-drag-start outer)
    (.on outer "pointerup" on-drag-end app)
    (.on outer "pointerupoutside" on-drag-end app)
    outer))