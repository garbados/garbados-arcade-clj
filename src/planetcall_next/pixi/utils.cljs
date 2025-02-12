(ns planetcall-next.pixi.utils
  (:require ["pixi.js" :refer [Text Container]]))

(defn ->text
  ([s style & {:as options :or {options {}}}]
   (new Text (clj->js (merge {:text s :style style} options)))))

(defn move-to [thing [x y]]
  (when x
    (set! (.-x thing) x))
  (when y
    (set! (.-y thing) y)))

(defn pivot-to [thing [x y]]
  (.pivot.set ^js/Object thing x y))

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
          (let [dx (-> ^js/Object event .-movement .-x)
                dy (-> ^js/Object event .-movement .-y)]
            (when horz
              (set! (.-x container) (+ (.-x container) dx)))
            (when vert
              (set! (.-y container) (+ (.-y container) dy)))))
        on-drag-start
        (fn []
          (when (.-visible container)
            (.stage.on ^js/Object app "pointermove" on-drag-move app)))
        on-drag-end
        (fn []
          (.stage.off ^js/Object app "pointermove" on-drag-move app))]
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
    (.stage.on ^js/Object app "pointerdown" on-drag-start app)
    (.stage.on ^js/Object app "pointerup" on-drag-end app)
    (.stage.on ^js/Object app "pointerupoutside" on-drag-end app)
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
    (.on ^js/Object outer "pointerdown" on-drag-start outer)
    (.on ^js/Object outer "pointerup" on-drag-end app)
    (.on ^js/Object outer "pointerupoutside" on-drag-end app)
    outer))

(defn zoomable-container [container & {:keys [max-zoom min-zoom]
                                       :or {max-zoom 3
                                            min-zoom 1}}]
  (let [outer (new Container)]
    (.addChild outer container)
    (set! (.-eventMode outer) "static")
    (js/addEventListener "wheel"
                         (fn [event]
                           (if (pos? (.-deltaY event))
                             (.scale.set outer (max min-zoom (- (.-scale.-x outer) 0.1)))
                             (.scale.set outer (min max-zoom (+ (.-scale.-x outer) 0.1))))))
    (.addChild outer container)
    outer))
