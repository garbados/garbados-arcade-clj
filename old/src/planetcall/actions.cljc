(ns planetcall.actions
  (:require [clojure.set :refer [difference intersection]]
            [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [planetcall.combat :as combat]
            [planetcall.games :as pg :refer [has-researched]]
            [planetcall.geometry :as geo]
            [planetcall.ideotech :as ideotech]
            [planetcall.improvements :as pi]
            [planetcall.regions :as pr]
            [planetcall.units :as pu]
            [planetcall.wonders :as pw]))

(s/def ::description string?)
(s/def ::validator s/spec?)
(s/def ::options (s/fspec :args (s/cat :game ::pg/game
                                       :player ::pg/player
                                       :rest (s/* any?))
                          :ret (s/coll-of any?)))
(s/def ::enabled? (s/fspec :args ::pg/game-player
                           :ret boolean?))
(s/def ::prompt (s/keys :req-un [::options
                                 ::validator
                                 ::description]))
(s/def ::prompts (s/coll-of ::prompt))
(s/def ::effect (s/fspec :args (s/cat :game ::pg/game
                                      :player ::pg/player
                                      :rest (s/* any?))
                         :ret ::pg/game))
(s/def ::cost (s/int-in 0 3))
(s/def ::action-detail
  (s/keys :req-un [::description
                   ::enabled?
                   ::prompts
                   ::effect
                   ::cost]))
(s/def ::action-listing
  (s/map-of keyword? ::action-detail))

;; IMPROVEMENT ACTIONS

(defn construct-improvement-factory
  "(factory :farm) => construct-farm"
  [improvement]
  {:pre [(s/or
          :wonder
          (s/valid? pw/wonders improvement)
          :improvement
          (s/valid? pi/improvements improvement))]
   :post [#(s/valid? ::effect %)]}
  (fn [game player coord]
    {:pre [(s/valid? ::pg/game game)
           ;; player is an int referring to a faction
           (s/valid? ::pg/player player)
           #(> (-> game :factions count) player)
           ;; coord refers to a space claimed by the player
           ;; or adjacent to such a space.
           (s/valid? ::geo/coord coord)
           (geo/intersects?
            (-> game :factions (get player) :claimed)
            (geo/get-adjacent-coords coord))]
     :post [#(s/valid? ::pg/game %)]}
    (-> game
        (pg/claim-space player coord)
        (pg/gain-experience player :industry 2)
        (pg/apply-eco-impact player 2)
        (assoc-in [:coord->space coord :improvement] improvement))))

(defn construct-improvement-action-details [enabled? improvement]
  {:pre [(s/or
          :wonder
          (s/valid? pw/wonders improvement)
          :improvement
          (s/valid? pi/improvements improvement))]
   :post [#(s/valid? ::action-listing %)]}
  (let [as-str
        (name improvement)
        as-name
        (string/join " " (string/split as-str #","))
        id
        (keyword (str "construct-" as-str))
        details
        {:description (str "Construct a " as-name " on a claimed space,"
                           "or an unclaimed space adjacent to your regions.")
         :enabled? enabled?
         :prompts [{:description (str "Where will you construct the " as-name "?")
                    :options (fn [game player]
                               (difference
                                (pg/claimed-and-adjacent game player)
                                (pg/find-improvements game player improvement)
                                (reduce into #{} (map :claimed (game :factions)))))
                    :validator ::geo/coord}]
         :effect (construct-improvement-factory improvement)
         :cost (if (= improvement :road) 1 2)}]
    [id details]))

(def basic-improvement-actions
  (map (partial construct-improvement-action-details (constantly true))
       [:farm :workblock :reactor :stockpile :road :laboratory]))

(def ideotech-improvement-actions
  (map (fn [[improvement shortcode]]
         (construct-improvement-action-details
          #(has-researched %1 %2 shortcode)
          improvement))
       [[:sensor "ms2"]
        [:temple "se2"]
        [:museum "ci2"]
        [:mine "i1"]
        [:bunker "m3a"]
        [:rocket-silo "m4a"]
        [:pulse-tower "ms4b"]
        [:condenser "i4d"]
        [:borehole "i4d"]
        [:refractor "i4d"]]))

;; CULTIVATE VEGETATION ACTIONS

(def vegetation-actions
  (map (fn [[vegetation shortcode]]
         (let [id 
               (->> vegetation name (str "cultivate-") keyword)
               details
               {:description (str "Cultivate " (name vegetation) " vegetation on a claimed space.")
                :enabled? #(pg/has-researched %1 %2 shortcode)
                :prompts [{:description "Where will you place the vegetation?"
                           :options (fn [game player]
                                      (-> game :factions (nth player) :claimed))
                           :validator ::geo/coord}]
                :effect (fn [game player coord]
                          (-> game
                              (pg/gain-experience player :ecology 2)
                              (assoc-in [:coord->space coord :vegetation] vegetation)))
                :cost 1}]
           [id details]))
       [[:forest "se2"]
        [:fungus "e2a"]]))

;; CREATE UNIT ACTIONS

(def register-design
  (let [filter-by-shortcode
        (fn [rules game player & _]
          (->> rules
               (filter
                (fn [[_ shortcode]]
                  (cond
                    (nil? shortcode)
                    true
                    (pg/has-researched game player shortcode)
                    true
                    :else
                    false)))
               (map first)))
        loadout-filter (partial filter-by-shortcode
                                [[:firearms nil]
                                 [:commander "m1"]
                                 [:railguns "m2a"]
                                 [:magspears "m3b"]
                                 [:disruptor "m4b"]
                                 [:missilier "m2b"]
                                 [:discharger "m3c"]
                                 [:starcaller "m4c"]
                                 [:hacker "s2a"]
                                 [:arbiter "s4a"]
                                 [:saboteur "s4d"]
                                 [:psi-beacon "c4a"]
                                 [:settler "i2b"]])
        chassis-filter-rules
        [[:infantry nil]
         [:speeder "m1"]
         [:windrider "ms3a"]
         [:jaeger "ms4a"]
         [:foil "im3a"]
         [:cruiser "im3a"]
         [:spikejet "im3b"]
         [:hovertank "im4a"]
         [:neograv "im4b"]
         [:loper "ec3a"]
         [:wormswarm "ec3a"]
         [:razorbeak "ec4b"]
         [:draconaut "ec4b"]]
        chassis-filter
        (fn [game player loadout]
          (let [chassis* (filter-by-shortcode chassis-filter-rules game player)
                heavy? (-> loadout pu/loadout->details :traits :heavy)]
            (if heavy?
              (filter #(-> % pu/chassis->details :traits :aerial) chassis*)
              chassis*)))
        mod-filter* (partial filter-by-shortcode
                             [[:observer "s2a"]
                              [:optical-camo "s4a"]
                              [:zero-trace "e2b"]
                              [:volunteer "c2b"]
                              [:psi-training "c3a"]
                              [:automaton "ci3b"]
                              [:durasteel "ci3b"]
                              [:trooper "im2"]
                              [:carrier "im3b"]
                              [:fissile-engine "ms3b"]
                              [:anti-materiel "ms3b"]
                              [:neural-dampener "ms4a"]])
        mod-filter (fn [game player _loadout _chassis & [mod]]
                     (let [options* (mod-filter* game player)
                           options (if mod
                                     (remove (partial = mod) options*)
                                     options*)]
                       (cons nil options)))]
    [:register-design
     {:description "Register a new unit design from available components."
      :enabled? (constantly true)
      :prompts [{:description "Which loadout will you use?"
                 :options loadout-filter
                 :validator ::pu/loadout}
                {:description "Which chassis will you use?"
                 :options chassis-filter
                 :validator ::pu/chassis}
                {:description "Which primary mod will you use?"
                 :options mod-filter
                 :validator (s/nilable ::pu/mod)}
                {:description "Which secondary mod will you use?"
                 :options mod-filter
                 :validator (s/nilable ::pu/mod)}]
      :effect (fn [game player loadout chassis mod1 mod2]
                (let [mods (set (filter (comp not nil?) [mod1 mod2]))
                      designs-path [:factions player :designs]
                      design {:loadout loadout :chassis chassis :mods mods}]
                  (update-in game designs-path (partial (comp set cons) design))))
      :cost 0}]))

(def forget-design
  [:forget-design
   {:description "Forget an old unit design."
    :enabled? (constantly true)
    :prompts [{:description "Which design will you forget?"
               :options #(get-in %1 [:factions %2 :designs])
               :validator ::pu/design}]
    :effect (fn [game player design]
              (pg/assoc-with game [:factions player :designs]
                             #(filter (partial (comp not =) design) %)
                             design))
    :cost 0}])

(def create-unit
  [:create-unit
   {:description "Create a unit from a saved design."
    :enabled? #(> 0 (count (pg/find-improvements %1 %2 :workblock)))
    :prompts [{:description "Which design will you use?"
               :options #(get-in %1 [:factions %2 :designs])
               :validator ::pu/design}
              {:description "Which region will build the unit?"
               :validator ::geo/coords
               :options
               (fn [game player design]
                 (let [workblocks (pg/find-improvements game player :workblock)
                       regions (->> (get-in game [:factions player :claimed])
                                    pr/get-regions
                                    (map #(geo/intersects? workblocks %)))
                       unit-cost (pu/get-design-cost design)]
                   (filter
                    (fn [region]
                      (let [stockpiles (->> (-> game
                                                :factions
                                                (get player)
                                                :stockpiles
                                                keys)
                                            (filter (partial contains? region)))
                            region-materials (->> stockpiles
                                                  (map
                                                   #(get-in game [:factions player :stockpiles % :materials]))
                                                  (reduce +))]
                        (> region-materials unit-cost)))
                    regions)))}
              {:description "Where will you construct the unit?"
               :validator ::geo/coord
               :options
               (fn [game player _design region]
                 (let [workblocks (pg/find-improvements game player :workblock)]
                   (intersection workblocks region)))}]
    :effect (fn [game player design region coord]
              ;; pay for the unit
              (let [stockpiles (filter (partial contains? region)
                                       (-> game
                                           :factions
                                           (get player)
                                           :stockpiles
                                           keys))
                    unit-cost (pu/get-design-cost design)
                    apportion-cost (fn [[game* cost] stockpile]
                                     (let [stockpile-path [:factions player :stockpiles stockpile :materials]
                                           materials (get-in game* stockpile-path)]
                                       (cond
                                         (= 0 cost)      (reduced game*)
                                         (= 0 materials) [game* cost]
                                         :else
                                         [(update-in game* stockpile-path dec)
                                          (dec cost)])))
                    unit (pu/design->unit design player)]
                (-> apportion-cost
                    (reduce [game unit-cost] (cycle stockpiles))
                    (update-in [:coord->units coord]
                               #(if %
                                  (cons unit %)
                                  [unit])))))
    :cost 2}])

(def disband-unit
  [:disband-unit
   {:description "Disband a unit, destroying it."
    :enabled? #(->> (pg/get-player-units %1 %2) count (< 0))
    :prompts [{:description "Which unit will you disband?"
               :options (fn [game player]
                          (->> (pg/get-player-units game player)
                               seq
                               (map (fn [[coord units]]
                                      (for [unit units] [coord unit])))
                               (reduce concat)))
               :validator (s/tuple ::geo/coord ::pu/unit)}]
    :effect (fn [game _ [coord unit]]
              (update-in game [:coord->units coord]
                         (fn [units]
                           (->> units
                                (filter #(not (= % unit)))
                                vec))))
    :cost 0}])

(def unit-actions
  [register-design forget-design create-unit disband-unit])

;; IMPROVEMENT ACTIONS

(def improvement-actions
  (map
   (fn [[id description [improvement distance strength]]]
     (let [is-enemy?
           #(or (= -1 %3)
                (= -1 (get-in %1 [:treaties #{%2 %3}])))
           has-only-enemies?
           (fn [game player coord]
             (->> coord
                  (pg/get-visible-units-at-coord game player)
                  (map :faction)
                  (map (partial is-enemy? game player))
                  #(and (every? true? %)
                        (seq %))))
           get-targets
           (fn [game player]
             (->> (pg/find-improvements game player improvement)
                  (map #(geo/get-coords-within % distance))
                  (reduce into #{})
                  (filter (partial has-only-enemies? game player))))]
       [id
        {:description description
         :enabled? (fn [game player]
                     (seq (pg/find-improvements game player improvement)))
         :prompts [{:description "Where will you center the strike?"
                    :options get-targets
                    :validator ::geo/coord}]
         :cost 1
         :effect
         (fn [game _player coord]
           (reduce
            #(combat/confront %1 strength [coord %2])
            game
            (range (count (pg/get-units-at-coord game coord)))))}]))
   [[:pulse-blast
     "Shunt energy through an entangled reaction mass -- causing devastation at a distance."
     [:pulse-tower 5 10]]
    [:launch-fusillade
     "Barrage a location with explosive projectiles."
     [:rocket-silo 7 7]]]))

;; CONSTRUCT WONDER ACTIONS

(defn keyword->name [-keyword]
  (let [words (string/split (name -keyword) #"-")
        -name (string/join " " (map string/capitalize words))]
    -name))

(def construct-wonder-actions
  (map
   (fn [[shortcode wonder]]
     (let [id (->> wonder name (str "construct-") keyword)
           -name (keyword->name wonder)
           details
           {:description (str "Construct the next stage of the " -name
                              " on or adjacent to a claimed space.")
            :enabled? (fn [game player]
                        (and
                         (pg/has-researched game player shortcode)
                         (not (pg/has-wonder-completed game player wonder))))
            :prompts [{:description (str "Where will you place the wonder?")
                       :options (fn [game player]
                                  (if-let [wonder-coord (pg/controls-wonder game player wonder)]
                                    #{wonder-coord}
                                    (pg/claimed-and-adjacent game player)))
                       :validator ::geo/coord}]
            :effect (fn [game player coord]
                      (-> game
                          (pg/gain-experience player :industry 2)
                          (pg/apply-eco-impact player 2)
                          (pg/place-wonder player coord wonder)))
            :cost 2}]
       [id details]))
   [["c3b"  :empath-guild]
    ["c4c"  :planetary-congress]
    ["ci4a" :grand-reliquary]
    ["ci4c" :beacon-institute]
    ["ec3b" :survivors-song]
    ["ec4c" :planetdream]
    ["i4a"  :earthscape]
    ["im4c" :dimensional-gate]
    ["ms4c" :planet-buster]
    ["s4b"  :singularity-collider]
    ["se4a" :ark-launchpad]
    ["se4c" :heavens-eye]]))

;; WONDER ACTIONS

;; TODO congressional measures
(def measure->details
  {:normalize-relations
   {:description
    "Bring all treaties one step closer to 0, ending all wars and alliances."
    :enabled? (constantly true)
    :effect
    (fn [game _player]
      (update game :treaties
              (fn [treaties]
                (->> treaties
                     seq
                     (map (fn [[pair n]]
                            [pair (cond (> n 0) (dec n)
                                        (< n 0) (inc n)
                                        :else   n)]))
                     (reduce merge {})))))}
   :regulate-archeology
   {:description "Ruins provide no benefit."
    :enabled?
    (fn [game _player]
      (not (pg/get-world-condition game :banned-ruins)))
    :effect
    (fn [game _player]
      (pg/apply-world-condition game :banned-ruins 10))}
   :regulate-industry
   {:description "Xenobogs, thermal vents, and rare metals benefits are halved."
    :enabled?
    (fn [game _player]
      (not (pg/get-world-condition game :banned-goods)))
    :effect
    (fn [game _player]
      (pg/apply-world-condition game :banned-goods 10))}})
(def measures (set (keys measure->details)))
(s/def ::measures measures)
(s/def ::measure-details (s/keys :req-un [::description
                                          ::effect]))

(def wonder-actions
  (map
   (fn [[wonder {:keys [name description prompts effect]}]]
     [name {:description description
            :enabled? (fn [game player]
                        (and
                         (pg/has-wonder-completed game player wonder)
                         (pg/controls-wonder game player wonder)))
            :prompts prompts
            :effect effect
            :cost 2}])
   [[:singularity-collider
     {:name :rend-creation
      :description
      "Tear a hole in spacetime, obliterating units and terrain in an area."
      :prompts
      [{:description "Where will you center the devastation?"
        :options
        (fn [game player]
          (->> (pg/find-improvements game player :singularity-collider)
               (map #(geo/get-coords-within % 7))
               (reduce into #{})))
        :validator ::geo/coord}]
      :effect
      (fn [game _player coord]
        (reduce
         (fn [game* coord]
           (reduce
            #(combat/confront %1 30 [coord %2])
            game*
            (-> game* :coord->units (get coord) count range)))
         game
         (->> (geo/get-adjacent-coords coord)
              (into #{coord})
              (pg/get-real-coords game))))}]
    [:heavens-eye
     {:name :create-miasma
      :description "Seed the clouds with miasmatic spores."
      :prompts
      [{:description "Where will you place the miasma?"
        :validator ::geo/coord
        :options
        (fn [game player]
          (->> (pg/find-improvements game player :heavens-eye)
               (map #(geo/get-coords-within % 7))
               (filter #(-> game :coord->space (get %) :miasma false?))
               (reduce into #{})))}]
      :effect
      (fn [game player coord]
        (-> game
            (assoc-in [:coord->space coord :miasma] true)
            (pg/gain-experience player :ecology 2)
            (pg/gain-experience player :contact 2)))}]
    [:planetary-congress
     {:name :call-vote
      :description "Convene the factions and force a consensus."
      :enabled?
      (fn [game _player]
        (not (pg/get-world-condition game :voted)))
      :prompts
      [{:description "Which measure will you put to a vote?"
        :validator ::measures
        :options
        (fn [game player]
          (filter
           #((-> % measure->details :enabled?) game player)
           measures))}]
      :effect
      (fn [game player measure]
        (let [effect (-> measure measure->details :effect)]
          (-> game
              (pg/apply-world-condition :voted 10)
              (effect player))))}]]))

;; IDEOTECH ACTIONS

(def ideotech-actions
  (map (fn [[shortcode id details]]
         [id
          (merge {:enabled? #(pg/has-researched %1 %2 shortcode)}
                 details)])
       [["e2b"
         :scrub-radiation
         {:description "Remove radiation from a controlled space."
          :prompts [{:description "Where will you scrub radiation?"
                     :validator ::geo/coord
                     :options
                     (fn [game player]
                       (filter
                        #(-> game :coord->space (get %) :radiation (> 0))
                        (pg/claimed-and-controlled game player)))}]
          :effect (fn [game player coord]
                    (-> game
                        (assoc-in [:coord->space coord :radiation] 0)
                        (pg/gain-experience player :ecology 5)))
          :cost 1}]
        ["e4c"
         :harmonize
         {:description "Align economic activity with biospheric contours."
          :prompts []
          :enabled? (fn [game player]
                     (and (pg/has-researched game player "e4c")
                          (not (some? (pg/get-faction-condition game player :plenitude)))))
          :effect (fn [game player]
                    (pg/apply-faction-condition game player :plenitude))
          :cost 1}]
        ["ci4b"
         :excavate
         {:description "Unearth the remnants of another era from Planet's rusted crust."
          :prompts [{:description "Where will you place the ruin?"
                     :validator ::geo/coord
                     :options
                     (fn [game player]
                       (filter
                        #(-> game :coord->space (get %) :feature (not= :ruin))
                        (pg/claimed-and-adjacent game player)))}]
          :effect (fn [game player coord]
                    (-> game
                        (assoc-in [:coord->space coord :feature] :ruin)
                        (pg/gain-experience player :industry 2)
                        (pg/gain-experience player :contact 2)
                        (pg/apply-eco-impact player 10)))
          :cost 1}]]))

;; DIPLOMACY ACTIONS

(def lower-treaty-action
  [:diminish-treaty
   {:description "Lower your treaty level with another faction, potentially beginning a war."
    :enabled? (fn [game player]
                (not (pg/has-researched game player "c4b")))
    :prompts [{:description "Which faction will you alienate?"
               :validator ::pg/player
               :options
               (fn [game player]
                 (filter #(and (not= % player)
                               (> (get-in game [:treaties #{player %}]) -1))
                         (-> game :factions count range)))}]
    :effect (fn [game player player2]
              (update-in game [:treaties #{player player2}] dec))
    :cost 1}])

(def raise-treaty-action
  [:expand-treaty
   {:description "Raise your treaty level with another faction, potentially ending a war."
    :enabled? (fn [game player]
                (or (pg/has-researched game player "c3c")
                    (and (some true?
                               (map
                                (fn [[pair _level]]
                                  (contains? pair player))
                                (:treaties game)))
                         (some true?
                               (for [player* (->> game :factions count range (filter #(not= player %)))
                                     :let [researched1 (get-in game [:factions player :researched])
                                           researched2 (get-in game [:factions player* :researched])
                                           conflicts (ideotech/count-conflicts researched1 researched2)
                                           treaty (get-in game [:treaties #{player player*}])]]
                                 (< treaty conflicts))))))
    :prompts [{:description "Which faction will you entreat?"
               :validator ::pg/player
               :options
               (fn [game player]
                 (for [player* (->> game :factions count range)
                       :when (and (not= player player*)
                                  (< (get-in game [:treaties #{player player*}]) 4))]
                   player*))}]
    :effect (fn [game player player2]
              (update-in game [:treaties #{player player2}] inc))
    :cost 1}])

(def diplomacy-actions [lower-treaty-action raise-treaty-action])

;; PASS ACTION

(def pass-action
  [:pass
   {:description "Spend an action point doing nothing. Hypothetically useful."
    :enabled? (constantly true)
    :prompts []
    :effect (fn [game _player] game)
    :cost 1}])

;; NOW THAT THE GANG'S ALL HERE...  

(def action->details
  (reduce into {}
          [basic-improvement-actions
           ideotech-improvement-actions
           vegetation-actions
           unit-actions
           improvement-actions
           construct-wonder-actions
           wonder-actions
           ideotech-actions
           diplomacy-actions
           [pass-action]]))

(s/def ::actions (-> action->details keys set))

(defn can-use [game player {:keys [enabled? prompts cost]}]
  {:pre [(s/valid? nat-int? cost)]}
  (let [action-points (get-in game [:turn-info :action-points])]
    (and (>= action-points cost)
         (enabled? game player)
         ;; test the first prompt for options if one exists
         (if (= 0 (count prompts))
           true
           (some any? (((comp :options first) prompts) game player))))))

(s/fdef can-use
  :args (s/cat :game ::pg/game
               :player ::pg/player
               :action ::action-detail)
  :ret boolean?)

(defn available-actions [game player]
  (->> action->details
       seq
       (filter #(->> % second (can-use game player)))
       (reduce merge {})))

(s/fdef available-actions
  :args (s/cat :game ::pg/game :player nat-int?)
  :ret (s/every-kv ::actions ::action-detail))

(defn do-action [game player id & args]
  (let [{:keys [cost effect prompts] :as action} (get action->details id)
        apply-effect #(apply effect % player args)]
    (if (and (can-use game player action)
             (= (count args) (count prompts))
             (every? (fn [[{:keys [validator]} arg]]
                       (s/valid? validator arg))
                     (map vector prompts args)))
      (-> game
          (update-in [:turn-info :action-points] - cost)
          apply-effect)
      (throw (ex-info "Cannot execute this action!"
                      {:action id
                       :args args
                       :valid? (map (fn [{:keys [validator]} arg]
                                      (s/explain-data validator arg))
                                    prompts
                                    args)})))))

(s/fdef do-action
  :args (s/cat :game ::pg/game
               :player ::pg/player
               :action ::actions
               :args (s/* any?))
  :ret ::pg/game)

(defn get-action-points [game player]
  {:pre [(s/valid? ::pg/game game)
         (s/valid? ::pg/player player)]
   :post [#(s/valid? nat-int? %)]}
  (+ 4
     (if (pg/has-researched game player "e3c") -1 0)
     (if (pg/has-researched game player "i3a") 1 0)
     (if (pg/has-researched game player "m4d") 1 0)
     (if (pg/has-wonder-completed game player :grand-reliquary) 1 0)))
