(ns arcade.core)

;; macros run in clj, during compilation
;; so cljs can use slurp
;; so long as it uses it at compilation
(defmacro inline-slurp [path]
  (clojure.core/slurp path))
