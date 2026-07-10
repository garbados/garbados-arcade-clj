(ns ambition.core
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.gen.alpha :as spec-gen]))

;;;;;;;;;;;;;;;;
;; BASIC DEFS ;;
;;;;;;;;;;;;;;;;

(def ACTIONS [:develop :muster :advance :despoil :pass]) ; FIXME are you sure about "pass"?
(spec/def ::action (set ACTIONS))

(def BOARD-SIZE 6)
(def COORDS
  (for [x (range BOARD-SIZE)
        y (range BOARD-SIZE)]
    [x y]))
(def PLAYERS #{0 1 2 3})
(def CLAIMANTS (into PLAYERS #{nil false}))
(def STARTS
  [[[2, 0], [3, 0]]
   [[2, 5], [3, 5]]
   [[0, 2], [0, 3]]
   [[5, 2], [5, 3]]])

(spec/def ::infra (spec/int-in 0 3))
(spec/def ::player PLAYERS)
(spec/def ::claimant
  (spec/with-gen
    (spec/spec #(contains? CLAIMANTS %))
    #(spec/gen CLAIMANTS)))
(spec/def ::space
  (spec/keys :req-un [::infra ::claimant]))

(spec/def ::unit #{1 2})
(spec/def ::dimension (spec/int-in 0 BOARD-SIZE))
(spec/def ::coord (spec/tuple ::dimension ::dimension))
(spec/def ::coords (spec/coll-of ::coord))
(spec/def ::spaces (spec/map-of ::coord ::space))
(spec/def ::units (spec/map-of ::coord (spec/nilable ::unit)))
(spec/def ::turn nat-int?)
(spec/def ::done? boolean?)
(spec/def ::meta (spec/keys :req-un [::turn ::done?]))

(def WORLD
  (reduce
   (fn [world [player coords]]
     (reduce
      #(-> %1
           (assoc-in [:spaces %2 :claimant] player)
           (assoc-in [:spaces %2 :infra] 1)
           (assoc-in [:units  %2] 1))
      world
      coords))
   {:spaces
    (reduce
     (fn [agg coord] (assoc agg coord {:infra 0 :claimant nil}))
     {} COORDS)
    :units {}
    :meta {:turn 0 :done? false}}
   (map-indexed vector STARTS)))

(spec/def ::world* (spec/keys :req-un [::spaces ::units ::meta]))
(spec/def ::world
  (spec/with-gen
    ::world*
    #(spec-gen/fmap (partial merge-with merge WORLD) (spec/gen ::world*))))

;;;;;;;;;;;;;;;;;;;;;
;; BASIC FUNCTIONS ;;
;;;;;;;;;;;;;;;;;;;;;

(defn get-claimant [world coord]
  (get-in world [:spaces coord :claimant]))

(spec/fdef get-claimant
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret ::claimant)

(defn get-infra [world coord]
  (get-in world [:spaces coord :infra] 0))

(spec/fdef get-infra
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret ::infra)

(defn get-unit [world coord]
  (get-in world [:units coord]))

(spec/fdef get-unit
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret (spec/nilable ::unit))

(def adjacent-coords
  (memoize
   (fn
     [[x y] coords distance]
     (filter
      (fn [[x* y*]]
        ;; diagonals are adjacent, so a square board feels more round
        (and (not (and (= x x*) (= y y*)))
             (>= distance (- x x*) (- distance))
             (>= distance (- y y*) (- distance))))
      coords))))

(spec/fdef adjacent-coords
  :args (spec/cat :coord ::coord
                  :coords ::coords
                  :distance pos-int?)
  :ret ::coords)

(defn adjacent-world-coords
  ([world coord]
   (adjacent-world-coords world coord 1))
  ([world coord distance]
   (adjacent-coords coord (keys (:spaces world)) distance)))

(spec/fdef adjacent-world-coords
  :args (spec/cat :world ::world
                  :coord ::coord
                  :distance (spec/? pos-int?))
  :ret ::coords)

(defn passable? [world coord]
  (not= false (get-in world [:spaces coord :claimant])))

(spec/fdef passable?
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret boolean?)

(defn distance-between [[x1 y1] [x2 y2]]
  (let [dx (abs (- x1 x2))
        dy (abs (- y1 y2))
        diagonals (min dx dy)
        straights (- (max dx dy) diagonals)]
    (+ diagonals straights)))

(spec/fdef distance-between
  :args (spec/cat :coord1 ::coord
                  :coord2 ::coord)
  :ret ::dimension)

(defn within-reach [world coord]
  (when-let [unit (get-unit world coord)]
    (adjacent-world-coords world coord unit)))

(spec/fdef within-reach
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret (spec/nilable ::coords))

(defn flanked? [world coord]
  (->> (:units world)
       (filter
        (fn [[coord* unit]]
          (and (some? unit)
               (not= (get-claimant world coord)
                     (get-claimant world coord*))
               (>= unit (distance-between coord coord*)))))
       count
       (<= 2)))

(spec/fdef flanked?
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret boolean?)

(defn can-advance-to? [world coord1 coord2]
  (when-let [unit (get-unit world coord1)]
    (or (>= unit (distance-between coord1 coord2))
        (nil? (get-unit world coord2))
        (and (not= (get-claimant world coord1)
                   (get-claimant world coord2))
             (flanked? world coord2)))))

(spec/fdef can-advance-to?
  :args (spec/cat :world  ::world
                  :coord1 ::coord
                  :coord2 ::coord)
  :ret (spec/nilable boolean?))

(defn valid-shunts [world coord]
  (when-let [unit (get-unit world coord)]
    (->> (adjacent-world-coords world coord)
         (filter
          (fn [coord*]
            (and
             ;; can't move where you can't go
             (passable? world coord*)
             (or
              ;; is the other space empty?
              (nil? (get-unit world coord*))
              (if (not= (get-claimant world coord)
                        (get-claimant world coord*))
                ;; could the shunted unit make a hostile advance?
                (flanked? world coord*)
                ;; is the one unit more powerful than the other?
                (> unit (get-unit world coord*)))))))
         seq)))

(spec/fdef valid-shunts
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret (spec/nilable ::coords))

(defn get-despoilable [world]
  (seq
   (filter
    (fn [coord]
      (and
       (passable? world coord)
       (nil? (get-claimant world coord))))
    (keys (:spaces world)))))

(spec/fdef get-despoilable
  :args (spec/cat :world ::world)
  :ret (spec/nilable ::coords))

;;;;;;;;;;;;;;;;;;;;;;
;; ACTION FUNCTIONS ;;
;;;;;;;;;;;;;;;;;;;;;;

(defn develop! [world coord]
  (update-in world [:spaces coord :infra] #(min 2 (inc %))))

(spec/fdef develop!
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret ::world)

(defn muster! [world coord]
  (let [infra (get-in world [:spaces coord :infra])
        unit (if (= 2 infra) 2 1)]
    (assoc-in world [:units coord] unit)))

(spec/fdef muster!
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret ::world)

(defn advance! [world [coord1 coord2]]
  (let [unit (get-unit world coord1)
        claimant1 (get-claimant world coord1)
        claimant2 (get-claimant world coord2)]
    (cond->
     (-> world
         (assoc-in [:units coord1] nil)
         (assoc-in [:units coord2] unit))
      (zero? (get-infra world coord1))
      (assoc-in [:spaces coord1 :claimant] nil)
      (not= claimant1 claimant2)
      (assoc-in [:spaces coord2 :claimant] claimant1))))

(spec/fdef advance!
  :args (spec/cat :world ::world
                  :coords (spec/tuple ::coord ::coord))
  :ret ::world)

(defn despoil! [world coord]
  (if (zero? (get-infra world coord))
    (let [may-shunt-to (valid-shunts world coord)
          world* (assoc-in world [:spaces coord :claimant] false)]
      (if (nil? may-shunt-to)
        (assoc-in world* [:units coord] nil)
        (advance! world* [coord (rand-nth may-shunt-to)])))
    (assoc-in world [:spaces coord :infra] 0)))

(spec/fdef despoil!
  :args (spec/cat :world ::world
                  :coord ::coord)
  :ret ::world)

(defn ^:no-stest pass! [world _player] world)

;;;;;;;;;;;;;;;;;;;;;;
;; OPTION FUNCTIONS ;;
;;;;;;;;;;;;;;;;;;;;;;

(defn can-develop? [world player]
  (->> (:spaces world)
       (filter
        (fn [[_coord {:keys [infra claimant]}]]
          (and (> 2 infra)
               (= player claimant))))
       (map first)
       seq))

(spec/fdef can-develop?
  :args (spec/cat :world ::world
                  :player ::player)
  :ret (spec/nilable ::coords))

(defn can-muster? [world player]
  (->> (:spaces world)
       (filter
        (fn [[coord {:keys [infra claimant]}]]
          (and (= player claimant)
               (> infra 0)
               (nil? (get-in world [:units coord])))))
       (map first)
       seq))

(spec/fdef can-muster?
  :args (spec/cat :world ::world
                  :player ::player)
  :ret (spec/nilable ::coords))

(defn can-advance? [world player]
  (->> (:units world)
       (filter
        (fn [[coord unit]]
          (and (= player (get-claimant world coord))
               (some? unit))))
       (map first)
       (map
        (fn [coord]
          (->> (within-reach world coord)
               (filter
                (fn [coord*] (can-advance-to? world coord coord*)))
               (map
                (fn [coord*] [coord coord*])))))
       (filter seq)
       (reduce concat [])))

(spec/fdef can-advance?
  :args (spec/cat :world ::world
                  :player ::player)
  :ret (spec/nilable (spec/coll-of (spec/tuple ::coord ::coord))))

(defn can-despoil? [world player]
  (->> (:units world)
       (filter
        (fn [[coord unit]]
          (and (= player (get-in world [:spaces coord :claimant]))
               (some? unit))))
       (map first)
       seq))

(spec/fdef can-despoil?
  :args (spec/cat :world ::world
                  :player ::player)
  :ret (spec/nilable ::coords))

;;;;;;;;;;;;;;;;;;;;
;; GAME FUNCTIONS ;;
;;;;;;;;;;;;;;;;;;;;

(def ACTION-METHODS
  {:develop {:can? can-develop? :do! develop!}
   :muster  {:can? can-muster?  :do! muster!}
   :advance {:can? can-advance? :do! advance!}
   :despoil {:can? can-despoil? :do! despoil!}
   :pass    {:can? (constantly [true])
             :do! (fn [world & _args] world)}})

(defn get-valid-actions [world player]
  (->> ACTION-METHODS
       (map
        (fn [[action {:keys [can?]}]]
          (when-let [options (seq (can? world player))]
            [action options])))
       (filter some?)
       (reduce #(apply assoc %1 %2) {})))

(spec/def :options/develop ::coords)
(spec/def :options/muster ::coords)
(spec/def :options/advance (spec/coll-of (spec/tuple ::coord ::coord)))
(spec/def :options/despoil ::coords)
(spec/def :options/pass (spec/coll-of true? :max-count 1))
(spec/def ::action-options
  (spec/keys :opt-un [:options/develop
                      :options/muster
                      :options/advance
                      :options/despoil
                      :options/pass]))
(spec/fdef get-valid-actions
  :args (spec/cat :world ::world
                  :player ::player)
  :ret ::action-options)

(defn begin-round [world]
  (update-in world [:meta :turn] inc))

(spec/fdef begin-round
  :args (spec/cat :world ::world)
  :ret ::world)

(defn end-round [world]
  (if-let [despoilable (get-despoilable world)]
    (despoil! world (rand-nth despoilable))
    (assoc-in world [:meta :done?] true)))

(spec/fdef end-round
  :args (spec/cat :world ::world)
  :ret ::world)

(defn eliminated? [world player]
  (->> (:spaces world)
       (map first)
       (filter
        (fn [coord]
          (= player (get-claimant world coord))))
       empty?))

(spec/fdef eliminated?
  :args (spec/cat :world ::world
                  :player ::player)
  :ret boolean?)

(defn count-scores [world]
  (->> (:spaces world)
       (map second)
       (filter
        (fn [{:keys [claimant]}]
          (contains? PLAYERS claimant)))
       (reduce
        (fn [scores {:keys [infra claimant]}]
          (update scores claimant + infra))
        {0 0 1 0 2 0 3 0})))

(spec/fdef count-scores
  :args (spec/cat :world ::world)
  :ret (spec/map-of ::player nat-int?))
