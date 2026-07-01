(ns arcade.stache
  "Very simple mustache-like parsing."
  (:require [clojure.string :as string]))

(defn render [template data]
  (reduce
   (fn [template [key value]]
     (string/replace template (str "{{" (name key) "}}") value))
   template
   data))
