(ns arcade.utils)

(defn indexOf [coll x]
  (.indexOf #?(:clj (seq coll) :cljs (to-array coll)) x))
