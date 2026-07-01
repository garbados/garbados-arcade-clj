(ns planetcall-next.web.utils)

(defn midpoint [& coords]
  (let [xs (map first coords)
        ys (map second coords)]
    [(js/Math.round (/ (reduce + 0 xs) (count xs)))
     (js/Math.round (/ (reduce + ys) (count ys)))]))
