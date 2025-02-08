(ns planetcall-next.rules.tech
  (:require
   [arcade.slurp :refer-macros [slurp->details]]
   [clojure.string :as string]))

(def ideotech->details*
  (merge
   (slurp->details "resources/planetcall/ideotech/ecology.edn")
   (slurp->details "resources/planetcall/ideotech/industry.edn")
   (slurp->details "resources/planetcall/ideotech/military.edn")))

(def ideograph
  (reduce
   (fn [ideograph [ideology ideo-details]]
     (reduce
      (fn [ideograph [level details]]
        (reduce
         (fn [ideograph [n detail]]
           (assoc-in ideograph [ideology level n] detail))
         ideograph
         (for [i (range (count details))
               :let [detail (nth details i)]]
           [i (assoc detail :n i)])))
      ideograph
      (group-by :level ideo-details)))
   {}
   (group-by :ideology (vals ideotech->details*))))

(def ideotech->details
  (reduce
   (fn [->details {tech-id :id :as detail}]
     (assoc ->details tech-id detail))
   {}
   (->> ideograph vals (map vals) flatten (map #(map second %)) flatten)))

(defn tech-name [{tech-id :id}]
  (->> (string/split (name tech-id) #"-")
       (map string/capitalize)
       (string/join " ")))
