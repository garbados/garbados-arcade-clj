(ns planetcall-next.rules.spaces 
  (:require
   [arcade.slurp :refer-macros [slurp->details]]))

(def prefix->details
  (slurp->details "resources/planetcall/spaces/prefixes.edn"))

(def suffix->details
  (slurp->details "resources/planetcall/spaces/suffixes.edn"))

(def improvement->details
  (merge
   (slurp->details "resources/planetcall/improvements/core.edn")
   (slurp->details "resources/planetcall/improvements/ideotech.edn")))

(def feature->details
  (slurp->details "resources/planetcall/spaces/features.edn"))

(def space-prefixes
  (set (keys prefix->details)))

(def space-suffixes
  (set (keys suffix->details)))

(def space-features
  (set (keys feature->details)))

(defn space-yield
  [{:keys [prefix suffix improvement feature]}
   & {:keys [prefix->details suffix->details improvement->details feature->details]
      :or {prefix->details prefix->details
           suffix->details suffix->details
           improvement->details improvement->details
           feature->details feature->details}}]
  (merge-with + {:food 0
                 :materials 0
                 :energy 0
                 :insight 0
                 :impact 0}
              (:yield (prefix->details prefix) {})
              (:yield (suffix->details suffix) {})
              (:yield (improvement->details improvement) {})
              (:yield (feature->details feature) {})))

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

(def space-gens
  {:default #(reduce
              (fn [spaces coord]
                (assoc spaces coord (gen-chaotic-space coord)))
              {}
              %)})
