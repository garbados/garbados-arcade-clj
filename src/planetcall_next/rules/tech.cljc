(ns planetcall-next.rules.tech
  (:require
   [arcade.slurp :refer-macros [slurp->details]]
   [clojure.set :as set]
   [clojure.string :as string]))

(def ideology-names
  [:military
   :industry
   :contact
   :ecology
   :science])

(def synergy-names
  [:science-military
   :military-industry
   :industry-contact
   :contact-ecology
   :ecology-science])

(def synergy->ideologies
  {:science-military [:science :military]
   :military-industry [:military :industry]
   :industry-contact [:industry :contact]
   :contact-ecology [:contact :ecology]
   :ecology-science [:ecology :science]})

(def ideology->opposites
  {:ecology [:military :industry :military-industry]
   :ecology-science [:industry]
   :contact [:science :military :science-military]
   :contact-ecology [:military]
   :industry [:ecology :science :ecology-science]
   :industry-contact [:science]
   :military [:contact :ecology :contact-ecology]
   :military-industry [:ecology]
   :science [:industry :contact :industry-contact]
   :science-military [:contact]})

(def ideotech->details*
  (merge
   (slurp->details "resources/planetcall/ideotech/ecology.edn")
   (slurp->details "resources/planetcall/ideotech/ecology-science.edn")
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
               :let [detail (nth details i)
                     detail* (assoc detail :n i)]]
           [i detail*])))
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

(def explain-key-names
  {:actions ["action" "actions"]
   :conditions ["condition" "conditions"]
   :improvement ["improvement" "improvements"]
   :abilities ["ability" "abilities"]
   :loadouts ["loadout" "loadouts"]
   :chassis ["chassis" "chassis"]
   :mods ["unit mod" "unit mods"]
   :wonders ["wonder" "wonders"]})

(defn explain-tech [{description :description
                     grants :grants
                     modifies :modifies
                     ideology :ideology
                     level :level
                     :or {description "TODO"}}]
  (let [grant-text
        (if (seq grants)
          (->> grants
               (reduce
                (fn [parts [k vs]]
                  (cons
                   (let [k-name
                         (if (= 1 (count vs))
                           (first (get explain-key-names k))
                           (second (get explain-key-names k)))
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
               (string/join "\n"))
          "")
        modifies-text
        (if modifies
          (let [s
                (->>
                 (flatten
                  (for [[k v] modifies]
                    (cond
                      (= k :actions)
                      (str "Actions: " v)
                      (= k :improvements)
                      (for [[improvement yields] v]
                        (str
                         (string/capitalize (name improvement)) ": "
                         (string/join
                          ", "
                          (for [[resource n] yields]
                            (str (string/capitalize (name resource)) " " n))))))))
                 (filter some?)
                 (map #(str "- " %))
                 (string/join "\n"))]
            (str "Modifies:\n" s))
          "")
        level-description (str (name ideology) " level " level)]
    (string/join "\n\n" (filter seq [level-description description grant-text modifies-text]))))

(defn tech-forbidden-by* [tech-id]
  (let [{ideology :ideology level :level} (ideotech->details tech-id)
        opposites (ideology->opposites ideology)]
    (set
     (for [[tech-id* {ideology* :ideology level* :level}] ideotech->details
           :when (and (contains? (set opposites) ideology*)
                      (> level* (- (if (contains? (set ideology-names) ideology*) 4 3) level)))]
       tech-id*))))

(def tech-forbidden-by (memoize tech-forbidden-by*))

(defn has-researched-tier? [known-tech ideology level]
  (if (contains? (set synergy-names) ideology)
    (let [ideologies (synergy->ideologies ideology)]
      (every? #(has-researched-tier? known-tech % (dec level)) ideologies))
    (let [candidates (set (map :id (vals (get-in ideograph [ideology level] {}))))]
      (some some? (set/intersection known-tech candidates)))))

(defn is-forbidden? [known-tech tech-id]
  (let [forbidden (tech-forbidden-by tech-id)]
    (seq (set/intersection known-tech forbidden))))

(defn may-research? [known-tech tech-id]
  (let [{ideology :ideology level :level} (ideotech->details tech-id)]
    (and
     (not (contains? known-tech tech-id))
     (if (= 1 level)
       true
       (has-researched-tier? known-tech ideology (dec level)))
     (not (is-forbidden? known-tech tech-id)))))

(defn get-researchable [known-tech]
  (set
   (filter
    (partial may-research? known-tech)
    (keys ideotech->details))))
