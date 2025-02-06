(ns planetcall-next.rules.games)

(def space-prefixes
  #{:rotting
    :volatile
    :shattered
    :preserved
    :ashen})

(def space-suffixes
  #{:mountain
    :canyon
    :mesa
    :steppe
    :marsh
    :ooze
    :wreckage
    :wastes})

(def space-features
  #{:vents
    :ruins
    :titan
    :xenobog})

(defn gen-space
  ([coord]
   (gen-space coord {}))
  ([[x y]
    {:keys [miasma fungus road prefix suffix feature improvement controller]}]
   {:coord [x y]
    :miasma miasma
    :fungus fungus
    :road road
    :prefix prefix
    :suffix suffix
    :feature feature
    :improvement improvement
    :controller controller}))

(defn gen-chaotic-space [coord]
  (gen-space coord {:miasma (rand-nth [true false])
                    :fungus (rand-nth [true true true false false])
                    :road (rand-nth [true false false false])
                    :prefix (rand-nth (seq space-prefixes))
                    :suffix (rand-nth (seq space-suffixes))
                    :feature (rand-nth (concat (repeatedly 5 (constantly nil)) (seq space-features)))}))

(def CHEEKY ["Red" "Blue" "Yellow" "Green" "Purple" "Orange"])

(defn gen-faction [i]
  {:i i
   :name (nth CHEEKY i)
   :designs #{}
   :resources {:food 0
               :materials 0
               :energy 0
               :insight 0}
   :research {:current nil
              :known #{}
              :experience {:military 0
                           :industry 0
                           :contact 0
                           :ecology 0
                           :science 0}}
   :conditions {}
   :claimed #{}
   :visible #{}
   :seen #{}})

(defn init-game [coords players]
  {:turn {:n 0
          :actions 0
          :phase nil}
   :world {:eco-damage 0
           :conditions {}}
   :wonders {}
   :treaties (reduce
              (fn [treaties faction-pair]
                (assoc treaties faction-pair 0))
              {}
              (for [i (range players)
                    j (range players)]
                #{i j}))
   :spaces (reduce
            (fn [spaces coord]
              (assoc spaces coord (gen-chaotic-space coord)))
            {}
            coords)
   :units {}
   :factions
   (reduce
    (fn [factions i]
      (assoc factions i (gen-faction i)))
    {}
    (range players))})