(ns planetcall.games
  (:require [clojure.set :refer [difference intersection]]
            [clojure.spec.alpha :as s]
            [clojure.test.check.generators :as gen]
            [planetcall.factions :as pf]
            [planetcall.geometry :as geo :refer [get-adjacent-to-region
                                                 get-coords-within]]
            [planetcall.ideotech :as pi :refer [ideologies]]
            [planetcall.improvements :refer [primary-resources]]
            [planetcall.wonders :as pw]
            [planetcall.spaces :as ps :refer [gen-unclaimed-space]]
            [planetcall.units :refer [design->unit] :as pu]))

(s/def ::coords (s/coll-of ::geo/coord :kind set?))

(s/def ::coord->space
  (s/map-of ::geo/coord ::ps/space))

(s/def ::coord->units
  (s/map-of ::geo/coord (s/coll-of ::pu/unit :kind vector?)))

(s/def ::eco-damage nat-int?)

(def conditions
  #{:voted
    :banned-ruins
    :banned-goods})
(s/def ::condition conditions)
(s/def ::conditions (s/map-of ::condition any?))

(s/def ::world (s/keys :req-un [::eco-damage
                                ::conditions]))

(s/def ::player nat-int?)
(s/def ::treaties (s/every-kv (s/coll-of ::player :kind set? :count 2)
                              (s/int-in -1 5)))
(s/def ::factions (s/coll-of ::pf/faction :kind vector?))
(s/def ::turn nat-int?) ;; (mod turn (count players)) => player
(s/def ::done? boolean?)
(s/def ::cause pw/wonders)
(s/def ::winners (s/nilable (s/coll-of (s/nilable ::cause))))

(s/def ::action-points nat-int?)
(s/def ::turn-info (s/keys :req-un [::action-points]))

;; base game s/def, used by init and mock functions
(s/def ::game*
  (s/keys :req-un [::coords
                   ::coord->space
                   ::coord->units
                   ::treaties
                   ::factions
                   ::world
                   ::turn
                   ::turn-info
                   ::done?
                   ::winners]))

(defn init-game [coord->space
                 coord->units
                 factions]
  {:pre [(s/valid? ::coord->space coord->space)
         (s/valid? ::coord->units coord->units)
         (s/valid? ::factions factions)]
   :post [#(s/valid? ::game* %)]}
  {:coords (-> coord->space keys set)
   :coord->space coord->space
   :coord->units coord->units
   :treaties
   (let [player-range (-> factions count range)
         faction-pairs
         (reduce
          (fn [pairs pair]
            (assoc pairs pair 0))
          {}
          (for [player1 player-range
                player2 player-range
                :when (not= player1 player2)]
            #{player1 player2}))]
     faction-pairs)
   :factions factions
   :world {:eco-damage 0 :conditions {}}
   :turn 0
   :turn-info {:action-points 0}
   :done? false
   :winners nil})

(defn init-mock-game []
  (let [coords (get-coords-within [0 0] 12)
        starts #{[-7 0]
                 [7 0]
                 [-7 7]
                 [7 -7]
                 [0 -7]
                 [0 7]}
        base-tech ["s1" "e1" "c1" "i1" "m1"]
        coord->units
        (->> (-> starts count range)
             (map
              (fn [coord player]
                {coord [(design->unit
                         {:loadout :firearms
                          :chassis :infantry
                          :mods #{}}
                         player)]})
              starts)
             (reduce into {}))
        factions (apply vector
                        (map (fn [coord tech]
                               {:designs pu/starting-designs
                                :claimed #{coord}
                                :wonders {}
                                :stockpiles {coord (reduce #(assoc %1 %2 2) {} primary-resources)}
                                :researched #{tech}
                                :current-research [nil 0]
                                :bonus-research 0
                                :researching {}
                                :experience (reduce #(assoc %1 %2 0) {} ideologies)
                                :conditions {}
                                :last-seen {:coord->space {}
                                            :coord->units {}}})
                             starts
                             (cycle base-tech)))
        coord->space (reduce
                      (fn [all coord]
                        (let [space* (gen-unclaimed-space)
                              space  (if (contains? starts coord)
                                       (assoc space* :improvement :stockpile)
                                       space*)]
                          (assoc all coord space)))
                      {}
                      coords)]
    (init-game coord->space coord->units factions)))

(s/fdef init-mock-game
  :args (s/cat)
  :ret ::game*)

;; game s/def used everywhere else -- with a mock generator
(s/def ::game (s/with-gen ::game* #(gen/return (init-mock-game))))
(s/def ::game-player (s/cat :game   ::game
                            :player ::player))

(defn get-real-coords [game coords]
  (intersection
   (game :coords)
   (set coords)))

(s/fdef get-real-coords
  :args (s/cat :game ::game :coords (s/coll-of ::geo/coord))
  :ret ::geo/coords)

(def get-real-adjacent
  (comp get-real-coords geo/get-adjacent-coords))

(defn claim-space [game player coord]
  (assoc-in game [:factions player :claimed]
            (into #{coord}
                  (get-in game [:factions player :claimed]))))

(s/fdef claim-space
  :args (s/cat :game ::game
               :player ::player
               :coord ::geo/coord)
  :ret ::game)

(defn assoc-with
  "Apply a function during an assoc-in. See also: clojure.core/update-in"
  ([thing path f]
   (assoc-with thing path f nil))
  ([thing path f not-found]
   (assoc-in thing path
             (f (get-in thing path not-found)))))

(defn gain-experience [game player ideology n]
  (update-in game [:factions player :experience ideology] + n))

(defn complete-research [game player shortcode]
  {:pre [(pi/may-research? (get-in game [:factions player :researched])
                           shortcode)]}
  (-> game
      (update-in [:factions player :researched] (partial into #{shortcode}))
      (assoc-in [:factions player :current-research] [nil 0])))

(s/fdef complete-research
  :args (s/cat :game ::game :player ::player :shortcode ::pi/shortcode)
  :ret ::game)

(defn needs-research? [game player]
  (-> game :factions (get player) :current-research first nil?))

(defn select-research [game player shortcode]
  {:pre [(pi/may-research? (get-in game [:factions player :researched])
                           shortcode)]}
  (assoc-in game [:factions player :current-research] [shortcode 0]))

(s/fdef select-research
  :args (s/cat :game ::game :player ::player :shortcode ::pi/shortcode)
  :ret ::game)

(defn has-researched [game player shortcode]
  (get-in game [:factions player :researched shortcode]))

(s/fdef has-researched
  :args (s/cat :game ::game :player ::player :shortcode ::pi/shortcode)
  :ret (s/nilable ::pi/shortcode))

(def transformational-wonders
  #{:dimensional-gate
    :planetdream
    :beacon-institute
    :ark-launchpad
    :planet-buster})

(defn has-wonder-completed [game player wonder]
  (let [transformational? (transformational-wonders wonder)
        max-stages (if transformational? 20 5)]
    (= max-stages
       (get-in game [:factions player :wonders wonder] 0))))

(s/fdef has-wonder-completed
  :args (s/cat :game ::game
               :player ::player
               :wonder ::pw/wonder))

(defn place-wonder [game player coord wonder]
  {:pre [(s/valid? ::game game)
         (s/valid? ::player player)
         (< player (-> game :factions count))
         (s/valid? ::geo/coord coord)
         (contains? (game :coord->space) coord)
         (contains?
          (-> game :factions (get player) :claimed get-adjacent-to-region)
          coord)
         (s/valid? ::pw/wonder wonder)
         (not (has-wonder-completed game player wonder))]
   :post [#(s/valid? ::game %)
          #(< 0 (-> % :factions (get player) :wonders (get wonder)))]}
  (let [stage (-> game :factions (get player) :wonders (get wonder 0))]
    (-> game
        (assoc-in [:factions player :wonders wonder] (inc stage))
        (assoc-in [:coord->space coord :improvement] wonder))))

(defn find-improvements [game player improvement]
  (set
   (filter
    #(-> game :coord->space (get %) :improvement (= improvement))
    (-> game :factions (nth player) :claimed))))

(defn controls-wonder [game player wonder]
  (first (find-improvements game player wonder)))

(defn claimed-and-adjacent [{:keys [coord->space factions]} player]
  (let [real-coords        (keys coord->space)
        adjacent-to-region (->> (get-in factions [player :claimed])
                                (map geo/get-adjacent-coords)
                                (reduce into #{}))]
    (intersection (set real-coords) adjacent-to-region)))

(defn adjacent-and-unclaimed [{:keys [coord->space factions]} player]
  (difference (claimed-and-adjacent {:coord->space coord->space
                                     :factions factions}
                                    player)
              (reduce into #{}
                      (for [player* (-> factions count range)]
                        (if (= player* player)
                          #{}
                          (-> factions (get player*) :claimed))))))

(defn is-controlled-unit? [player unit]
  (or (= player (:faction unit))
      (= player (get-in unit [:conditions :subverted]))))

(defn get-player-units [game player]
  (reduce
   (fn [all [coord units]]
     (assoc all coord units))
   {}
   (for [[coord units] (:coord->units game)
         :let [own-units (filter (partial is-controlled-unit? player) units)]
         :when (> (count own-units) 0)]
     [coord own-units])))

(s/fdef get-player-units
  :args ::game-player
  :ret ::coord->units)

(defn update-player-units
  [game player f & args]
  (reduce
   (fn [game* [coord units]]
     (assoc-in game* [:coord->units coord] units))
   game
   (for [[coord units] (:coord->units game)
         :let [units*
               (for [unit units]
                 (when (is-controlled-unit? player unit)
                   (apply f coord unit args)))]
         :when [(some some? units*)]]
     [coord (vec (map #(or %1 %2) units* units))])))

(s/fdef update-player-units
  :args (s/cat :game ::game
               :player ::player
               :f (s/fspec
                   :args (s/cat :coord ::geo/coord
                                :unit ::pu/unit
                                :rest (s/* any?))
                   :ret ::pu/unit)
               :rest (s/* any?))
  :ret ::game)

(defn get-planet-units [game]
  (get-player-units game -1))

(defn get-unit-index [game coord unit]
  (->> (-> game :coord->units (get coord) count range)
       (filter (fn [i] (= unit (get-in game [:coord->units coord i]))))
       first))

(defn get-unit-by-index [game [coord i]]
  (get-in game [:coord->units coord i]))

(defn update-unit [game coord unit unit*]
  (let [i (get-unit-index game coord unit)]
    (assoc-in game [:coord->units coord i] unit*)))

(defn claimed-and-controlled [game player]
  (let [claimed (get-in game [:factions player :claimed])
        controlled (keys (get-player-units game player))]
    (into claimed controlled)))

(defn get-units-at-coord [game coord]
  (get-in game [:coord->units coord] []))

(defn get-visible-units-at-coord [game player coord]
  (get-in game [:factions player :last-seen :coord->units coord] []))

(defn is-unit-visible [coord observed-coords unit]
  (let [hidden? ((comp :hidden :traits) unit)
        observed? (observed-coords coord)]
    (cond
      (nil? unit)             false
      (and hidden? observed?) true
      hidden?                 false
      :else                   true)))

(defn get-visible [game player]
  {:pre [(or (s/valid? ::game game)
             (s/explain ::game game))
         (s/valid? ::player player)]
   :post [#(s/valid? ::pf/last-seen %)]}
  (let [get-real-coords* (partial get-real-coords game)
        claimed (get-in game [:factions player :claimed])
        player-units (get-player-units game player)
        units-coords (keys player-units)
        visible (->> (concat claimed units-coords)
                     (reduce
                      #(->> (geo/get-coords-within %2 2)
                            (into #{%2})
                            (into %1))
                      #{})
                     (reduce into)
                     get-real-coords*)
        observed-coords (->> (concat
                              (map
                               (fn [[coord units]]
                                 (let [perceptive? (some (comp :perceptive :traits) units)
                                       modifier (if perceptive? 2 1)]
                                   (into #{coord}
                                         (geo/get-coords-within coord modifier))))
                               player-units)
                              (map
                               (fn [coord]
                                 (let [improvement (get-in game [:coord->space coord :improvement])
                                       sensor? (= improvement :sensor)
                                       modifier (if sensor? 2 1)]
                                   (into #{coord}
                                         (geo/get-coords-within coord modifier))))
                               claimed))
                            (reduce into)
                            get-real-coords*)]
    {:coord->space
     (reduce
      (fn [all coord] (assoc all coord (get-in game [:coord->space coord])))
      {}
      visible)
     :coord->units
     (reduce
      (fn [all coord]
        (let [units (filter
                     #(or (= (:faction %) player)
                          (is-unit-visible coord observed-coords %))
                     (get-units-at-coord game coord))]
          (if (empty? units)
            all
            (assoc all coord units))))
      {}
      visible)}))

(defn update-visible
  "Merge the most recent visibility report with the last, overwriting any coords that have changed."
  [game player]
  (assoc-in game [:factions player :last-seen] (get-visible game player)))

(s/fdef update-visible
  :args ::game-player
  :ret ::game)

(defn apply-faction-condition
  ([game player condition]
   (apply-faction-condition game player condition true))
  ([game player condition value]
   (assoc-in game [:factions player :conditions condition] value)))

(defn get-faction-condition
  [game player condition]
  (get-in game [:factions player :conditions condition]))

(defn apply-eco-impact
  [game player impact]
  {:pre [(s/valid? int? impact)]}
  (-> game
      (update-in [:world :eco-damage]
                 (fn [damage]
                   (max 0 (+ damage impact))))
      (gain-experience player :ecology (max 0 (* -1 impact)))))

(defn apply-world-condition
  ([game condition value]
   (assoc-in game [:world :conditions condition] value))
  ([game condition]
   (apply-world-condition game condition true)))

(defn get-world-condition
  [game condition]
  (get-in game [:world :conditions condition]))

(defn diminish-world-conditions
  [game]
  (let [update-condition
        (fn [value]
          (if (number? value)
            (if (> value 1)
              ;; reduce value; 2 -> 1
              (dec value)
              ;; nullify value; 1 -> nil
              nil)
            value))]
    (reduce
     (fn [game condition]
       (update-in game [:world :conditions condition] update-condition))
     game
     (keys (get-in game [:world :conditions])))))

(defn how-did-player-win?
  "Return the transformational wonder that a player built to win. Returns nil if the player has not won."
  [game player]
  (->> transformational-wonders
       (filter (partial has-wonder-completed game player))
       first))

(defn cataclysm-level [{:keys [eco-damage]}]
  {:pre [(s/valid? ::eco-damage eco-damage)]
   :post [#(s/valid? int? %)]}
  (int (/ eco-damage 1000)))

(defn record-player-victories [game]
  (let [winners (map (partial how-did-player-win? game)
                     (-> game :factions count range))
        cataclysm? (<= 4 (cataclysm-level (:world game)))
        winners? (some some? winners)]
    (cond
      cataclysm?
      (merge game {:done? true})
      winners?
      (merge game {:winners winners
                   :done? true})
      :else
      game)))

(defn get-space-movement-cost
  [{:keys [factions coord->space]}
   coord
   {:keys [traits faction]}]
  (let [{:keys [elevation terrain improvement]}
        (coord->space coord)]
    (cond
      (and (= :road improvement)
           (contains? (get-in factions [faction :claimed])
                      coord))
      0
      (or (traits :all-terrain)
          (< 0 elevation))
      1
      :else
      (inc (.indexOf (ps/attribute->levels :terrain) terrain)))))

(s/fdef get-space-movement-cost
  :args (s/cat :game ::game
               :coord ::geo/coord
               :unit ::pu/unit)
  :ret nat-int?)

(defn is-enemy? [game player1 player2]
  (or (and (not= -1 player1) (= -1 player2))
      (not= player1 player2)
      (= -1 (get-in game [:treaties #{player1 player2}]))))

(def is-friendly? (complement is-enemy?))

(defn is-enemy-coord? [game faction coord]
  (->> (get-units-at-coord game coord)
       (map :faction)
       ((juxt some every?) (partial is-enemy? game faction))
       (reduce #(and %1 %2))))

(def is-friendly-coord? (complement is-enemy-coord?))