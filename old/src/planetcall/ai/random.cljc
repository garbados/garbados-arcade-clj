(ns planetcall.ai.random
  (:require [planetcall.games :as pg]
            [planetcall.actions :as pa]
            [planetcall.abilities :as pb]
            [planetcall.ideotech :as pi]
            [clojure.spec.alpha :as s]))

(defn choose-research [game player]
  (if (pg/needs-research? game player)
    (let [researched (get-in game [:factions player :researched])
          can-research (vec (pi/can-research researched))]
      (if (empty? can-research)
        game
        (let [research (rand-nth can-research)]
          (pg/select-research game player research))))
    game))

(defn choose-action [game player]
  (let [available (pa/available-actions game player)
        action (-> available keys rand-nth)
        choices (->> (get-in available [action :prompts])
                     (reduce
                      (fn [all {:keys [options]}]
                        (let [result (apply (partial options game player) all)]
                          (conj all result)))
                      [])
                     (map (comp rand-nth vec)))]
    [action choices]))

(defn choose-ability [game unit-path]
  (let [ability (rand-nth (pb/can-use game unit-path))
        {prompts :prompts} (pb/ability->details ability)
        choices
        (reduce
         (fn [args {options-fn :options}]
           (cons (apply options-fn game unit-path args) args))
         '()
         prompts)]
    [ability choices]))

(s/fdef choose-action
  :args (s/cat :game ::pg/game
               :player ::pg/player)
  :ret ::pg/game)
