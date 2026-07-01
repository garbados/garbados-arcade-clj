(ns planetcall-next.pixi.utils
  (:require
   ["@pixi/ui" :refer [Button]]
   ["pixi.js" :refer [BitmapText Container Text TextStyle Graphics]]
   [planetcall-next.web.colors :as colors]))

(defn ->text
  ([s style & {:as options :or {options {}}}]
   (new Text (clj->js (merge {:text s :style style} options)))))

(defn ->bitmap-text
  ([s style & {:as options :or {options {}}}]
   (new BitmapText (clj->js (merge {:text s :style style} options)))))

(defn move-to [thing [x y]]
  (when x
    (set! (.-x thing) x))
  (when y
    (set! (.-y thing) y)))

(defn pivot-to [thing [x y]]
  (.pivot.set ^js/Object thing x y))

(defn move-below [above below & [padding]]
  (let [y (+ (.-y above) (.-height above) (or padding 0))]
    (set! (.-y below) y)))

(defn move-right [left right & [padding]]
  (let [y (+ (.-x left) (.-width left) (or padding 0))]
    (set! (.-x right) y)))

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
          (let [dx (-> ^js/Object event .-movement .-x (* (/ 1 (.-parent.-scale.-x ^js/Object container))))
                dy (-> ^js/Object event .-movement .-y (* (/ 1 (.-parent.-scale.-y ^js/Object container))))]
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

(defn create-button [text & {:keys [top right on-hover on-out on-click font-size border-width]
                             :or {font-size 30
                                  top 6
                                  right 6}}]
  (let [inner-container (new Container)
        outer-container (new Container)
        style (new TextStyle (clj->js {:fontSize font-size :fill colors/WHITE}))
        text (->text text style)
        bg-w (+ (* 2 right) (.-width text))
        bg-h (+ (* 2 top) (.-height text))
        bg (new Graphics)
        draw-bg (fn [gfx color]
                  (.rect gfx 0 0 bg-w bg-h)
                  (.fill gfx color)
                  (when border-width
                    (.stroke gfx (clj->js {:color colors/WHITE
                                           :width border-width}))))
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
    (move-to text [right top])
    (set! (.-enabled button) true)
    (.addChild inner-container bg text)
    (.addChild outer-container inner-container)
    (.onHover.connect button hover-button)
    (.onOut.connect button reset-button)
    (.onDown.connect button click-button)
    (.onUp.connect button hover-button)
    (.onUpOut.connect button reset-button)
    outer-container))
