(ns planetcall-next.utils)

(defn remove-nth [coll n]
  (let [v (vec coll)]
    (concat
     (subvec v 0 n)
     (subvec v (inc n)))))
