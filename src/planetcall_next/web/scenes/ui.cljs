(ns planetcall-next.web.scenes.ui 
  (:require
   [clojure.string :as string]
   [planetcall-next.web.colors :as colors]
   [planetcall-next.web.tooltips :as tooltips]))

(set! *warn-on-infer* false)

(defn create-vert-text-objects
  [scene x y strings
   {:keys [top left color border] :or {top 7 left 7 color colors/BLACK border colors/WHITE}}]
  (let [container (.add.container scene x y)
        text-pairs
        (first
         (reduce
          (fn [[text-pairs prev-rect] s]
            (if prev-rect
              (let [this-text (.add.text scene left (+ top (.-height prev-rect) (.-y prev-rect)) s)
                    this-rect (.add.rectangle scene
                                              0
                                              (- (.-y this-text) top)
                                              (+ (* 2 left)
                                                 (.-width this-text))
                                              (+ (* 2 top)
                                                 (.-height this-text))
                                              color)]
                [(cons [this-text this-rect] text-pairs)
                 this-rect])
              (let [this-text (.add.text scene left top s)
                    this-rect (.add.rectangle scene 0 0 (+ (* 2 left) (.-width this-text)) (+ (* 2 top) (.-height this-text)) color)]
                [(cons [this-text this-rect] text-pairs)
                 this-rect])))
          [[] nil]
          strings))]
    (.add container (clj->js (reverse (reduce concat [] text-pairs))))
    (doseq [rect (map second text-pairs)]
      (.setOrigin rect 0))
    (when border
      (doseq [rect (map second text-pairs)]
        (.setStrokeStyle rect 3 border)))
    {:container container
     :text-pairs text-pairs})) 1

(defn create-horz-text-objects
  [scene x y strings
   {:keys [top left color border] :or {top 7 left 7 color colors/BLACK border colors/WHITE}}]
  (let [container (.add.container scene x y)
        text-pairs
        (first
         (reduce
          (fn [[text-pairs prev-rect] s]
            (if prev-rect
              (let [this-text (.add.text scene (+ left (.-width prev-rect) (.-x prev-rect)) top s)
                    this-rect (.add.rectangle scene (+ (.-width prev-rect) (.-x prev-rect)) 0 (+ (* 2 left) (.-width this-text)) (+ (* 2 top) (.-height this-text)) color)]
                [(cons [this-text this-rect] text-pairs)
                 this-rect])
              (let [this-text (.add.text scene left top s)
                    this-rect (.add.rectangle scene 0 0 (+ (* 2 left) (.-width this-text)) (+ (* 2 top) (.-height this-text)) color)]
                [(cons [this-text this-rect] text-pairs)
                 this-rect])))
          [[] nil]
          strings))]
    (.add container (clj->js (reverse (reduce concat [] text-pairs))))
    (doseq [rect (map second text-pairs)]
      (.setOrigin rect 0))
    (when border
      (doseq [rect (map second text-pairs)]
        (.setStrokeStyle rect 3 border)))
    {:container container
     :text-pairs text-pairs}))

(defn draw-file-menu [scene x y]
  ;; TODO handlers
  (create-vert-text-objects scene x y ["New Game" "Save Game" "Load Game" "Main Menu"] {}))

(defn draw-tab-bar
  [scene x y
   tab-keys
   {:keys [on-hover on-leave on-click active]
    :or {on-hover identity on-leave identity on-click identity}}
   & horz-opts]
  (let [{text-pairs :text-pairs
         :as tab-bar}
        (create-horz-text-objects scene x y (map (comp string/capitalize name) tab-keys) horz-opts)
        tab-rects (zipmap (reverse tab-keys) text-pairs)
        prev-rect (atom (when-let [[_ rect] (get tab-rects active)] rect))]
    (doseq [[tab-key [_ rect :as text-pair]] tab-rects]
      (.setInteractive rect)
      (when (= active tab-key)
        (.setFillStyle rect colors/BLUE))
      (.on rect "pointerover" #(when (not= @prev-rect rect)
                                 (on-hover tab-key text-pair)
                                 (.setFillStyle rect colors/DIM-GRAY)))
      (.on rect "pointerout" #(when (not= @prev-rect rect)
                                (on-leave tab-key text-pair)
                                (.setFillStyle rect colors/BLACK)))
      (.on rect "pointerdown" #(let [last-rect @prev-rect]
                                 (on-click tab-key text-pair)
                                 (when last-rect
                                   (.setFillStyle last-rect colors/BLACK))
                                 (reset! prev-rect rect)
                                 (.setFillStyle rect colors/BLUE))))
    tab-bar))

(defn create-ui-scene [scene & {:keys [active] :or {active :map}}]
  (let [map-scene (.scene.get scene "map")
        tech-scene (.scene.get scene "tech")
        wiki-scene (.scene.get scene "wiki")
        scenes {:map map-scene
                :tech tech-scene
                :wiki wiki-scene}
        active-scene (atom (get scenes active))
        file-menu (atom nil) ; initialized later
        swap-to
        (fn [tab-key]
          (if (not= tab-key :file)
            (let [prev-scene @active-scene
                  new-scene (get scenes tab-key)]
              (.setVisible (:container @file-menu) false)
              (when (not= prev-scene new-scene)
                (.scene.switch prev-scene new-scene)
                (reset! active-scene new-scene)))
            (.setVisible (:container @file-menu) true)))
        tab-bar
        (draw-tab-bar scene 0 0 [:file :map :tech :wiki]
                      {:active active
                       :on-click (fn [tab-key _] (swap-to tab-key))})
        {space-tooltip :container
         update-space-tooltip :update
         reset-space-tooltip :reset}
        (tooltips/make-space-tooltip scene)
        {update-unit-tooltip :update
         reset-unit-tooltip :reset}
        (tooltips/make-unit-tooltip scene)]
    (.setVisible space-tooltip false)
    (reset! file-menu
            (draw-file-menu scene 0 (.-height (second (first (:text-pairs tab-bar))))))
    (.setVisible (:container @file-menu) false)
    (.scene.launch scene (name active))
    (.events.on map-scene "tilemove"
                (juxt update-space-tooltip update-unit-tooltip)
                scene)
    (.events.on map-scene "tilereset"
                (juxt reset-unit-tooltip reset-space-tooltip)
                scene)))
