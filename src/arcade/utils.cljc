(ns arcade.utils)

(defn indexOf [coll x]
  (.indexOf #?(:clj (seq coll) :cljs (to-array coll)) x))

(defn contains-v? [coll x]
  (nat-int? (indexOf coll x)))
