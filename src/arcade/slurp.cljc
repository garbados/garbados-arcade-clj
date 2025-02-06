(ns arcade.slurp)

;; macros run in clj, during compilation
;; so cljs can use slurp
;; so long as it uses it at compilation
(defmacro inline-slurp [path]
  (clojure.core/slurp path))

(defmacro slurp->details [s]
  (reduce
   #(assoc %1 (:id %2) %2)
   {}
   (clojure.edn/read-string
    (clojure.core/slurp s))))