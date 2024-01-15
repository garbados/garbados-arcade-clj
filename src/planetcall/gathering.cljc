(ns planetcall.gathering
  (:require [clojure.spec.alpha :as s]
            [planetcall.games :as pg]
            [planetcall.geometry :as geo]
            [planetcall.ideotech :as pi]
            [planetcall.improvements :refer [improvement->details
                                             primary-resources]]
            [planetcall.regions :as pr]
            [planetcall.spaces
             :refer [get-space-upkeep get-space-yield]
             :as ps]
            [planetcall.units :as pu]))

(def improvement->adjacency
  {:condenser :food
   :borehole  :material
   :refractor :energy
   :academy   :knowledge})

(defn improve-yields [yield]
  (into {}
        (for [[resource value] (seq yield)]
          (if (> value 0)
            [resource 1]
            [resource 0]))))

(defn get-space-ideoyield
  [{:keys [coord->space world factions] :as game} player coord]
  {:pre [;; game is invalid because it's in-flight
         (s/valid? ::pg/player player)
         (s/valid? ::geo/coord coord)]
   :post [#(s/valid? ::ps/yield %)]}
  (let [space (get coord->space coord)
        primary (-> space :improvement improvement->details :primary)
        base-yield (get-space-yield space)
        adjacencies-yield
        (reduce
         (fn [yield adj-coord]
           (if (get-in factions [player :claimed adj-coord])
             (let [space (get coord->space adj-coord)
                   improvement (:improvement space)]
               (merge-with
                +
                yield
                (when-let [resource (improvement->adjacency improvement)]
                  (when (-> yield (get resource 0) (> 0))
                    {resource 2}))
                (when (pg/has-researched game player "e4a")
                  (let [primary* (-> improvement improvement->details :primary)
                        resource (-> space :feature ps/feature->resource)]
                    (when (= primary* resource)
                      {resource 2})))))
             yield))
         base-yield
         (geo/get-adjacent-coords coord))
        ideoyield
        (reduce
         (fn [yield [shortcode effect]]
           (if (pg/has-researched game player shortcode)
             (if (map? effect)
               (apply merge-with + yield
                      (for [[resource bonus] (seq effect)]
                        (when (-> yield (get resource 0) (> 0))
                          {resource bonus})))
               (merge-with + yield
                           (effect game player coord yield)))
             yield))
         adjacencies-yield
         [["s1" {:knowledge 1}]
          ["s2b" {:knowledge 2}]
          ["s3b" {:material 2}]
          ["s3c" {:energy 2}]
          ["s4c" {:knowledge 3}]
          ["se2"
           (fn [game _player coord yield]
             (when (= :forest (get-in coord->space [coord :vegetation]))
               (improve-yields yield)))]
          ["se3a"
           (fn [game _player coord yield]
             (when (= :forest (get-in coord->space [coord :vegetation]))
               (improve-yields yield)))]
          ["se3b"
           (fn [game _player coord _yield]
             (when (= :reactor (get-in coord->space [coord :improvement]))
               {:energy 2}))]
          ["se4b"
           (fn [game _player coord yield]
             (when (= :forest (get-in game [:coord->space coord :vegetation]))
               (let [modifier (->> (geo/get-adjacent-coords coord)
                                   #(= :fungus (get-in coord->space [% :vegetation]))
                                   (some true?)
                                   #(if % 2 1))]
                 (apply merge-with +
                        (for [_ (range modifier)]
                          (improve-yields yield))))))]
          ["e3a"
           (fn [game _player coord yield]
             (when (= :fungus (get-in coord->space [coord :vegetation]))
               (improve-yields yield)))]
          ["e4d"
           (fn [game _player coord yield]
             (when (= :fungus (get-in coord->space [coord :vegetation]))
               (apply merge-with +
                      (for [_ (range 2)]
                        (improve-yields yield)))))]
          ["ec4a"
           (fn [game _player coord _yield]
             (when (= :temple (get-in coord->space [coord :improvement]))
               {:knowledge 2}))]
          ["i3c"
           (constantly (when primary {primary 1}))]
          ["i4c"
           (constantly (when primary {primary 2}))]])
        cataclysm (pg/cataclysm-level world)
        cataclysm-yield
        (into {}
              (for [resource [:food :material :energy]
                    :let [yield (get ideoyield resource 0)]
                    :when (< 0 yield)]
                [resource (- cataclysm)]))
        final-yield
        (merge-with
         #(max 0 (- %1 %2))
         ideoyield
         cataclysm-yield)]
    final-yield))

(defn get-region-yields [game player region]
  {:pre [;; game is not subject to spec as it is mid-updates
         (s/valid? ::pg/player player)
         (s/valid? ::geo/coords region)]
   :post [#(s/valid? ::ps/yield %)]}
  (let [space-yields (map (partial get-space-ideoyield game player) region)]
    (reduce (partial merge-with +) {} space-yields)))

(defn get-region-upkeeps [{:keys [coord->space]} _player region]
  {:pre [(s/valid? ::geo/coords region)]
   :post [#(s/valid? ::ps/upkeep %)]}
  (reduce
   (fn [upkeeps coord]
     (let [upkeep (get-space-upkeep (get coord->space coord))]
       (merge-with + upkeeps upkeep)))
   {}
   region))

(defn get-region-stockpiles [game player region]
  {:pre [(s/valid? ::geo/coords region)]
   :post [#(s/valid? ::geo/coords %)]}
  (filter (partial contains? region)
          (keys (get-in game [:factions player :stockpiles]))))

(defn get-trade-multiplier [game player]
  (reduce
   (fn [sum [ok? effect]]
     (if ok?
       (effect sum)
       sum))
   1
   [[(pg/has-researched game player "c1")
     (partial + 1)]
    [(pg/has-researched game player "c2a")
     (partial + 1)]
    [(pg/has-researched game player "c3c")
     (partial + 1)]
    [(pg/has-researched game player "c4b")
     (partial + 3)]
    [(pg/has-wonder-completed game player :empath-guild)
     (partial * 2)]]))

(defn get-trade-income [game player]
  (let [multiplier (get-trade-multiplier game player)]
    (->> (:treaties game)
         seq
         (filter
          (fn [[pair n]]
            (and (contains? pair player)
                 (> n 0))))
         (map second)
         (reduce +)
         (* multiplier))))

(defn divide-trade-income [game player]
  (let [trade (get-trade-income game player)
        stockpiles (-> game :factions (get player) :stockpiles keys)]
    (reduce (fn [game coord]
              (let [stockpiled (get-in game [:factions player :stockpiles coord])
                    stockpiled* (into {}
                                      (map (fn [[resource x]]
                                             [resource (inc x)])
                                           (seq stockpiled)))]
                (assoc-in game [:factions player :stockpiles coord] stockpiled*)))
            game
            (take trade (cycle stockpiles)))))

(defn divide-surplus [game player region resource surplus]
  (let [op (if (> 0 surplus) - +)
        get-path #(vec [:factions player :stockpiles % resource])
        coords (filter
                (fn [coord]
                  (let [path (get-path coord)
                        stored (get-in game path)]
                    (if (= - op)
                      (< 0 stored)
                      true)))
                (get-region-stockpiles game player region))]
    (if (or (zero? surplus) (-> coords count zero?))
      [game (> 0 surplus)]
      (let [[game* surplus*]
            (reduce
             (fn [[game surplus] coord]
               (let [path (get-path coord)
                     stored (get-in game path)]
                 (if (or
                      (zero? surplus)
                      (and (= - op) (zero? stored)))
                   (reduced [game surplus])
                   [(assoc-in game path (op stored 1))
                    (op surplus -1)])))
             [game surplus]
             (cycle coords))
            shortage? (> 0 surplus*)]
        (if shortage?
          (divide-surplus game* player region resource surplus*)
          [game* shortage?])))))

(s/fdef divide-surplus
  :args (s/cat :game ::pg/game
               :player ::pg/player
               :region ::geo/coords
               :resource primary-resources
               :surplus int?)
  :ret (s/cat :game ::pg/game
              :shortage? boolean?))

(defn divide-unit-upkeep [game player]
  (let [upkeep (->> (pg/get-player-units game player)
                    seq
                    second
                    (reduce concat)
                    (map #(-> % :design pu/get-design-upkeep))
                    (reduce + 0))
        claimed (-> game :factions (nth player) :claimed)
        [game* shortage?] (divide-surplus game player claimed :energy (* -1 upkeep))]
    (if shortage?
      (pg/apply-faction-condition game* player :scarcity)
      (pg/apply-faction-condition game* player :scarcity false))))

(defn do-regional-gather [game player region]
  {:post [#(s/valid? (s/cat :game ::pg/game :knowledge nat-int?) %)]}
  (let [yields (get-region-yields game player region)
        upkeeps (get-region-upkeeps game player region)
        [game* shortage?]
        (reduce
         (fn [[game shortage?] resource]
           (let [yield* (get yields resource 0)
                 yield (if shortage?
                         (-> yield* (/ 2) int)
                         yield*)
                 upkeep (get upkeeps resource 0)
                 surplus (- yield upkeep)
                 [game* shortage?*] (divide-surplus game player region resource surplus)]
             [game*
              shortage?*]))
         [game false]
         [:food :material :energy])
        knowledge* (get yields :knowledge 0)
        knowledge (if shortage?
                    (int (/ knowledge* 2))
                    knowledge*)]
    [game* knowledge]))

(defn process-yields-and-upkeeps [game player]
  (let [regions (pr/get-regions (get-in game [:factions player :claimed]))
        [game* new-knowledge]
        (reduce
         (fn [[game knowledge] region]
           (let [[game* knowledge*] (do-regional-gather game player region)]
             [game* (+ knowledge knowledge*)]))
         [game 0]
         regions)
        [research old-knowledge]
        (get-in game* [:factions player :current-research])]
    (-> game*
        (pg/gain-experience player :science new-knowledge)
        (assoc-in [:factions player :current-research]
                  [research (+ old-knowledge new-knowledge)]))))

(defn limit-stockpiles [game player]
  (let [stockpile-max (if (pg/has-researched game player "i3b")
                        20
                        10)
        limit-stockpile
        (fn [[coord resources]]
          [coord (into {}
                       (for [[resource value] (seq resources)]
                         [resource (cond
                                     (< value 0) 0
                                     (> value stockpile-max) stockpile-max
                                     :else value)]))])
        limited-stockpiles
        (into {}
              (map
               limit-stockpile
               (-> game :factions (nth player) :stockpiles seq)))]
    (assoc-in game [:factions player :stockpiles] limited-stockpiles)))

(defn get-impact [game player coord]
  (let [space (get-in game [:coord->space coord])
        improvement (space :improvement)
        base-impact (get improvement :impact 0)]
    (reduce
     (fn [impact [ok? effect]]
       (if ok? (effect impact) impact))
     base-impact
     [[(pg/has-researched game player "se3b")
       (if (= improvement :reactor) dec identity)]
      [(pg/has-researched game player "se4b")
       (fn [sum]
         (let [adjacent-to-fungus?
               (some true?
                     (->> (geo/get-adjacent-coords coord)
                          (map #(get-in game [:coord->space %]))
                          (filter (comp not nil?))
                          (map (comp #(= % :fungus) :vegetation))))
               modifier (if adjacent-to-fungus? 2 1)]
           (- sum modifier)))]
      [(pg/has-researched game player "e4d")
       (if (= (space :vegetation) :fungus) dec identity)]
      [(pg/has-researched game player "ec4a")
       (if (= improvement :temple) (comp dec dec) identity)]
      [(pg/has-researched game player "i3c")
       inc]
      [(pg/has-researched game player "i4c")
       (comp inc inc)]])))

(defn process-impact [game player]
  (let [impact (->> (get-in game [:factions player :claimed])
                    (map (partial get-impact game player))
                    (reduce +))]
    (pg/apply-eco-impact game player impact)))

(s/fdef process-impact
  :args (s/cat :game ::pg/game
               :player ::pg/player)
  :ret ::pg/game)

(defn process-current-research [game player]
  (let [[shortcode knowledge*] (-> game :factions (nth player) :current-research)]
    (if shortcode
      (let [[ideocode tier _] (pi/unpack-shortcode shortcode)
            ideologies (map #(-> % str pi/code->ideology) ideocode)
            clear-experience
            (fn [game*]
              (reduce #(assoc-in %1 [:factions player :experience %2] 0) game* ideologies))
            knowledge
            (+ knowledge*
               (->> ideologies
                    (map #(get-in game [:factions player :experience %] 0))
                    (reduce +))
               (get-in game [:factions player :bonus-research]))
            xp-needed (* tier 10)
            remainder (- knowledge xp-needed)]
        (if (> 0 remainder)
          (-> game
              clear-experience
              (assoc-in [:factions player :current-research] [shortcode knowledge])
              (assoc-in [:factions player :bonus-research] 0))
          (-> game
              clear-experience
              (pg/complete-research player shortcode)
              (assoc-in [:factions player :current-research] [nil 0])
              (assoc-in [:factions player :bonus-research] remainder))))
      game)))

(defn diminish-unit-conditions [game player]
  (let [if-dec
        #(if %
           (min 0 (dec %))
           0)
        dec-cooldowns
        (fn [cooldowns]
          (reduce
           (fn [cooldowns* [ability cooldown]]
             (assoc-in cooldowns* ability (if-dec cooldown)))
           cooldowns
           cooldowns))
        reset-movement
        (fn [unit]
          (if (= 0 (get-in unit [:conditions :stunned] 0))
            (assoc unit :movement (:speed unit))
            unit))
        update-unit
        #(-> %2
             reset-movement
             (update-in [:conditions :stunned] if-dec)
             (update-in [:conditions :reinforced] if-dec)
             (update :conditions dissoc :subverted)
             (update :conditions dissoc :intercepting)
             (update :cooldowns dec-cooldowns))]
    (pg/update-player-units game player update-unit)))

(defn diminish-faction-conditions [game player]
  (update-in game [:factions player :conditions] dissoc :plenitude))

(defn do-gather-phase [game player]
  (-> game
      (divide-trade-income player)
      (process-yields-and-upkeeps player)
      (divide-unit-upkeep player)
      (limit-stockpiles player)
      (process-impact player)
      (process-current-research player)
      (diminish-unit-conditions player)
      (diminish-faction-conditions player)
      (pg/update-visible player)))

(s/fdef do-gather-phase
  :args (s/cat :game ::pg/game
               :player ::pg/player)
  :ret ::pg/game)
