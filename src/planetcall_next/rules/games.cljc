(ns planetcall-next.rules.games 
  (:require
   [planetcall-next.rules.spaces :as spaces]))

;; faction names that match default player colors
(def CHEEKY [:red :blue :yellow :green :purple :orange])

(defn gen-faction [i & {:keys [colors]
                        :or {colors CHEEKY}}]
  {:i i
   :name (name (nth colors i))
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

(defn init-game
  [coords players & {:keys [space-gen]
                     :or {space-gen :default}}]
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
   :spaces ((get spaces/space-gens space-gen) coords)
   :units {}
   :factions
   (reduce
    (fn [factions i]
      (assoc factions i (gen-faction i)))
    {}
    (range players))})

(defn claim-space [game faction coord]
  (-> game
      (update-in [:factions faction :claimed] into coord)
      (assoc-in [:spaces coord :controller] faction)))

(defn place-improvement [game coord improvement]
  (assoc-in game [:spaces coord :improvement] improvement))

(defn realize-unit [game unit]
  (assoc-in game [:units (:id unit)] unit))

(defn harm-unit [game unit n]
  (-> game
      (update-in [:units (:id unit) :integrity] - n)
      (update-in [:units (:id unit) :integrity] max 0)))

(defn heal-unit [game unit n]
  (-> game
      (update-in [:units (:id unit) :integrity] + n)
      (update-in [:units (:id unit) :integrity] min (:max-integrity unit))))

(defn destroyed? [unit]
  (zero? (:integrity unit)))
