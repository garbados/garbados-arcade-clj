(ns arcade.utils)

(defn indexOf [coll x]
  (.indexOf #?(:clj coll :cljs (to-array coll)) x))

(defn contains-v? [coll x]
  (nat-int? (indexOf coll x)))

(defn indexOfId [coll x]
  (indexOf (map :id coll) (:id x)))

(defn contains-id? [coll x]
  (contains-v? (map :id coll) (:id x)))
