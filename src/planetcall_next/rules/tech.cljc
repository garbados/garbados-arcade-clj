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

(defn explain-tech [{description :description
                     grants :grants
                     ideology :ideology
                     level :level
                     n :n
                     :or {description "TODO"}}]
  (if (seq grants)
    (->> grants
         (reduce
          (fn [parts [k vs]]
            (cons
             (let [k-name (name k)
                   k-name
                   (if (< 1 (count vs))
                     (apply str (subvec (vec k-name) 0
                                        (dec (count k-name))))
                     k-name)
                   k-name (str (string/capitalize k-name) ":")
                   v-s
                   (string/join
                    ", "
                    (for [v vs]
                      (->> (-> (name v)
                               (string/split #"-"))
                           (map string/capitalize)
                           (string/join " "))))]
               [k-name v-s])
             parts))
          [])
         (map #(str "- " (string/join " " %)))
         (cons "Grants:")
         (cons "")
         (cons description)
         (cons "")
         (cons (str (name ideology) " level " level))
         (string/join "\n"))
    ""))
