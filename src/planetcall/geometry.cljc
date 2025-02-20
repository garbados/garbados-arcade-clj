(ns planetcall.geometry
  (:require [clojure.spec.alpha :as s]
            [clojure.set :refer [intersection]]))

(s/def ::coord (s/cat :q int? :r int?))
(s/def ::coords (s/coll-of ::coord :kind set?))

(defn get-adjacent-coords
  "Returns coordinates for hexes "
  [coord]
  {:pre [(s/valid? ::coord coord)]
   :post [#(s/valid? ::coords %)]}
  (let [[q r] coord]
    [[q (inc r)]
     [(dec q) r]
     [q (dec r)]
     [(inc q) (inc r)]
     [(inc q) r]
     [(inc q) (dec r)]]))

;; (def get-adjacent-coords (memoize get-adjacent-coords*))

(defn get-adjacent-to-region [region]
  {:pre [(s/valid? ::coords region)]
   :post [#(s/valid? ::coords %)]}
  (reduce (fn [all coord]
            (into all (get-adjacent-coords coord)))
          #{}
          region))

(defn intersects?* [set1 set2]
  (seq (intersection set1 set2)))

;; just guessing, but i'd rather this be fast than cheap
(def intersects? (memoize intersects?*))

(defn get-coords-delta [coord1 coord2]
  {:pre [(s/valid? ::coord coord1)
         (s/valid? ::coord coord2)]
   :post [#(s/valid? ::coord %)]}
  (map - coord1 coord2))

(defn get-coords-distance [coord1 coord2]
  {:pre [(s/valid? ::coord coord1)
         (s/valid? ::coord coord2)]
   :post [#(s/valid? nat-int? %)]}
  (reduce max (map #(Math/abs %) (get-coords-delta coord1 coord2))))

(defn get-coords-within [center n]
  {:pre [(s/valid? ::coord center)
         (s/valid? pos-int? n)]
   :post [#(s/valid? ::coords %)]}
  (let [-n (- n)]
    (reduce into
            #{}
            (for [q (range -n (+ 1 n))]
              (let [less-r (max -n (- 0 q n))
                    more-r (+ 1 (min n (- 0 q -n)))]
                (map
                 #(map + center [q %])
                 (range less-r more-r)))))))

(defn lerp [a b t]
  {:pre [(s/valid? int? a)
         (s/valid? int? b)
         (s/valid? number? t)]
   :post [#(s/valid? int? %)]}
  (int (+ a (* t (- b a)))))

(defn get-coords-between [coord1 coord2]
  {:pre [(s/valid? ::coord coord1)
         (s/valid? ::coord coord2)]
   :post [#(s/valid? ::coords %)]}
  (let [distance (get-coords-distance coord1 coord2)]
    (for [i (range 1 distance)]
      (let [t (/ i distance)]
        (map #(lerp %1 %2 t) coord1 coord2)))))

(defn get-coords-reachable [coord->space start movement]
  (reduce
   (fn [[visited fringes] _]
     (reduce
      (fn [[prev-visited new-fringes] coord]
        (let [next-fringes (->> coord
                                get-adjacent-coords
                                (filter #(not (contains? prev-visited %)))
                                (filter #(contains? coord->space %)))]
          [(map #(assoc prev-visited % coord) next-fringes)
           (reduce into new-fringes next-fringes)]))
      [visited #{}]
      fringes))
   [{start nil} #{start}]
   (range 1 movement)))