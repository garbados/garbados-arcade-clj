(ns planetcall.regions
  (:require [clojure.spec.alpha :as s]
            [clojure.set :refer [union intersection]]
            [planetcall.geometry
             :refer [intersects?
                     get-adjacent-coords]
             :as geo]))

(s/def ::regions (s/coll-of ::geo/coords))

(defn count-intersections [sets subset]
  (reduce + (map #(if (intersects? subset %) 1 0) sets)))

(defn merge-regions [regions region]
  (if (empty? regions)
    [region]
    (if (some identity (map #(intersects? region %) regions))
      (map (fn [old-region]
             (if (intersects? old-region region)
               (union old-region region)
               old-region))
           regions)
      (cons region regions))))

(defn reduce-regions [regions]
  (let [intersections
        (map #(count-intersections regions %) regions)
        too-many-intersections
        (some true? (map #(< 1 %) intersections))]
    (if too-many-intersections
      ;; some regions intersect. merge them recursively
      (reduce-regions (reduce merge-regions [] regions))
      ;; no regions intersect
      regions)))

(defn get-regions
  "Reduce a set of coordinates into contiguous regions."
  [coords]
  {:pre [(s/valid? ::geo/coords coords)]
   :post [#(s/valid? ::regions %)]}
  (letfn [(get-chunk [coord] (union #{coord} (get-adjacent-coords coord)))
          (get-region [coord] (intersection coords (get-chunk coord)))]
    (reduce-regions (map get-region coords))))

(defn contiguous? [coords]
  {:pre [(s/valid? ::geo/coords coords)]
   :post [boolean?]}
  (let [regions (get-regions coords)]
    (= 1 (count regions))))
