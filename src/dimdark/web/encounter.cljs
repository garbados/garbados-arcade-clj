(ns dimdark.web.encounter 
  (:require [arcade.text :as text]
            [clojure.string :as string]
            [dimdark.abilities :as a]
            [dimdark.encounters :as e]
            [reagent.core :as r]
            [dimdark.core :as d]))

(def turn-stages #{:pre-selection :ability-selection :target-selection :do-ability :next-turn})

(defn pre-selection-view [-encounter -stage creature]
  (let [encounter @-encounter
        monster? (e/is-monster? encounter creature)
        env-effects (get encounter (if monster? :monsters-env :kobolds-env))
        [encounter* {:keys [stats] :as creature*}] (e/env-effects-tick encounter creature)
        creature** (update creature* :effects (partial e/expand-rolled-effects stats))
        effects (:effects creature**)
        {:keys [health] :as creature***} (e/resolve-instant-effects (e/turn-effects-tick creature**))
        effects* (:effects creature***)
        effects-lines
        (filter
         some?
         (concat
          [(when (contains? env-effects :jawtrapped)
             [:p (str "Caught in a jawtrap's bite!")])
           (when (contains? env-effects :mawtrapped)
             [:p (str "Caught in a mawtrap's snare!")])
           (when (contains? effects :damage)
             [:p (str "Suffered " (:damage effects) " damage from traps!")])
           (when (contains? effects :burning)
             [:p (str "Burned for " d/burn-damage " damage!")])
           (when (contains? effects :bleeding)
             [:p (str "Bled for " d/bleed-damage " damage!")])
           (when (contains? effects :mending)
             [:p (str "Regenerated " (:mending effects) " health!")])
           (when (contains? effects :poisoned)
             [:p (str "Suffered " (:poisoned effects) " damage from poison!")])]
          (for [effect d/diminishing-effects
                :let [expired? (and (contains? effects effect)
                                    (not (contains? effects* effect)))]
                :when expired?]
            [:p (str "No longer affected by " (text/normalize-name effect))])))]
    (if (seq effects-lines)
      [:div.content
       (for [line effects-lines] line)
       (when (zero? health)
         [:p (str (-> creature :name text/normalize-name) " has fainted!")])
       [:button.button.is-fullwidth
        {:on-click #(do
                      (reset! -encounter
                              (e/remove-dead-monsters
                               (e/assoc-creature encounter* creature* creature***)))
                      (if (zero? health)
                        (reset! -stage :next-turn)
                        (reset! -stage :ability-selection)))}
        "Proceed"]]
      (do (reset! -encounter (e/assoc-creature encounter* creature* creature***))
          (reset! -stage :ability-selection)
          [:<>]))))

(defn action-selection-view [-encounter -stage -ability creature]
  (let [encounter @-encounter]
    (if (e/is-monster? encounter creature)
      (do (reset! -ability
                  (rand-nth
                   (for [ability (e/get-usable-abilities encounter creature)
                         :when (seq (e/get-possible-targets encounter creature ability))]
                     ability)))
          (reset! -stage :target-selection)
          [:<>])
      [:<>
       [:h5 "Choose an ability to use:"]
       [:div.columns
        (for [ability (e/get-usable-abilities @-encounter creature)
              :let [{:keys [description traits]} (a/ability->details ability)]]
          ^{:key ability}
          [:div.column
           [:button.button.is-fullwidth.has-tooltip-arrow
            {:on-click #(do (reset! -ability ability)
                            (reset! -stage :target-selection))
             :data-tooltip
             (string/join "\n" [description (string/join "; " (map text/normalize-name traits))])}
            (text/normalize-name ability)]])]])))

(defn target-selection-view [-encounter -stage -ability -target creature]
  (let [encounter @-encounter
        ability @-ability
        targets (e/get-possible-targets @-encounter creature ability)]
    (if (e/is-monster? encounter creature)
      (let [targets (e/get-possible-targets encounter creature ability)
            needs-target? (-> ability a/ability->details a/needs-target?)
            target (if needs-target? (rand-nth targets) targets)]
        (reset! -target target)
        (reset! -stage :do-ability)
        [:<>])
      (if (a/needs-target? (a/ability->details ability))
        [:<>
         [:p "Select a target for this ability:"]
         [:div.columns
          (for [target targets]
            ^{:key target}
            [:div.column
             [:button.button.is-fullwidth
              {:on-click #(do (reset! -target target)
                              (reset! -stage :do-ability))}
              (text/normalize-name (:name target))]])]]
        [:<>
         [:p "This ability will affect:"]
         [:ul
          (for [target targets]
            ^{:key target}
            [:li (text/normalize-name (:name target))])]
         [:div.columns
          [:div.column
           [:button.button.is-primary.is-fullwidth
            {:on-click #(do (reset! -target targets)
                            (reset! -stage :do-ability))}
            "Proceed"]]
          [:div.column
           [:button.button.is-secondary.is-fullwidth
            {:on-click #(do (reset! -ability nil)
                            (reset! -stage :ability-selection))}
            "Reset"]]]]))))

(defn do-action-view [-encounter -stage -ability -target creature]
  (let [encounter @-encounter
        ability @-ability
        target @-target
        [creature* encounter* impacts] (e/calc-impacts encounter creature ability target)]
    (if (seq impacts)
      (let [{:keys [self-effects kobolds-env monsters-env]} impacts
            [creature** _ encounter**] (e/resolve-impacts encounter* creature* impacts)]
        [:div.content
         [:p (str (text/normalize-name (:name creature)) " used " (text/normalize-name ability) "!")]
         [:h1 [:strong "Hit!"]]
         [:h5 "Effects"]
         [:ul
          (for [line (sort
                      (flatten
                       (for [creature (filter (complement keyword?) (keys impacts))
                             :let [effects (get impacts creature)]]
                         (for [[effect magnitude] effects]
                           (str (text/normalize-name (:name creature)) " <- " (text/normalize-name effect) ": " magnitude)))))]
            ^{:key line}
            [:li line])
          (when self-effects
            (for [[effect magnitude] self-effects]
              ^{:key effect}
              [:li (-> creature** :name text/normalize-name)" <- " (text/normalize-name effect) ": " magnitude]))
          (when kobolds-env
            (for [[effect magnitude] kobolds-env]
              ^{:key effect}
              [:li "Kobold environs <- " (text/normalize-name effect) ": " magnitude]))
          (when monsters-env
            (for [[effect magnitude] monsters-env]
              ^{:key effect}
              [:li "Monster environs <- " (text/normalize-name effect) ": " magnitude]))
          (for [kobold (:kobolds encounter**)
                :when (zero? (:health kobold))]
            [:p (-> kobold :name text/normalize-name) " has fainted!"])
          (for [monster (:monsters encounter**)
                :when (zero? (:health monster))]
            [:p (-> monster :name text/normalize-name) " has fainted!"])]
         [:button.button.is-primary
          {:on-click #(do (reset! -encounter encounter**)
                          (reset! -stage :next-turn))}
          "Proceed"]])
      [:div.content
       [:h3 "Whiff!"]
       [:p (str (text/normalize-name (:name creature)) "'s use of " (text/normalize-name ability) " missed.")]
       [:button.button.is-primary.is-fullwidth
        {:on-click #(do (reset! -encounter encounter*)
                        (reset! -stage :next-turn))}
        "Proceed"]])))

(defn turn-view [-encounter -stage -ability -target creature]
  (case @-stage
    :pre-selection [pre-selection-view -encounter -stage creature]
    :ability-selection [action-selection-view -encounter -stage -ability creature]
    :target-selection [target-selection-view -encounter -stage -ability -target creature]
    :do-ability [do-action-view -encounter -stage -ability -target creature]
    :next-turn (do (swap! -encounter e/next-turn)
                   (reset! -stage :pre-selection)
                   [:<>])))

(defn next-turn-button [-encounter]
  [:button.button.is-primary.is-fullwidth
   {:on-click #(swap! -encounter (comp e/next-turn e/remove-dead-monsters))}
   "Proceed"])

(defn impacts-view [-encounter creature ability target]
  (if-let [impacts (e/calc-impacts @-encounter creature ability target)]
    (let [{:keys [kobolds-env monsters-env]} impacts
          creatures (flatten (filter (fn [[creature _]] (not (keyword? creature))) impacts))
          effect-lines
          (sort
           (flatten
            (for [creature creatures
                  :let [effects (get impacts creature)]]
              (for [[effect magnitude] effects]
                (str (text/normalize-name (:name creature)) " <- " (text/normalize-name effect) ": " magnitude)))))]
      [:div.content
       [:p (str (text/normalize-name (:name creature)) " used " (text/normalize-name ability) "!")]
       [:h1 [:strong "Hit!"]]
       [:h5 "Effects"]
       [:ul
        (for [line effect-lines]
          ^{:key line}
          [:li line])
        (when kobolds-env
          (for [[effect magnitude] kobolds-env]
            ^{:key effect}
            [:li "Kobold environs <- " (text/normalize-name effect) ": " magnitude]))
        (when monsters-env
          (for [[effect magnitude] monsters-env]
            ^{:key effect}
            [:li "Monster environs <- " (text/normalize-name effect) ": " magnitude]))]
       [next-turn-button -encounter]])
    [:div.content
     [:h3 "Whiff!"]
     [:p (str (text/normalize-name (:name creature)) "'s use of " (text/normalize-name ability) " missed.")]
     [next-turn-button -encounter]]))

(defn kobold-turn-view [-encounter kobold -ability -target]
  (let [ability @-ability
        target @-target]
   (cond
     (and ability target)
     [impacts-view -encounter kobold ability target]
     (and (some? ability)
          (nil? target))
     (let [targets (e/get-possible-targets @-encounter kobold ability)]
       (if (a/needs-target? (a/ability->details ability))
         [:<>
          [:p "Select a target for this ability:"]
          [:div.columns
           (for [target targets]
             ^{:key target}
             [:div.column
              [:button.button.is-fullwidth
               {:on-click #(reset! -target target)}
               (text/normalize-name (:name target))]])]]
         [:<>
          [:p "This ability will affect:"]
          [:ul
           (for [target targets]
             ^{:key target}
             [:li (text/normalize-name (:name target))])]
          [:div.columns
           [:div.column
            [:button.button.is-primary.is-fullwidth
             {:on-click #(reset! -target targets)}
             "Proceed"]]
           [:div.column
            [:button.button.is-secondary.is-fullwidth
             {:on-click #(reset! -ability nil)}
             "Reset"]]]]))
     :else
     [:<>
      [:h5 "Choose an ability to use:"]
      [:div.columns
       (for [ability (e/get-usable-abilities @-encounter kobold)
             :let [{:keys [description traits]} (a/ability->details ability)]]
         ^{:key ability}
         [:div.column
          [:button.button.is-fullwidth.has-tooltip-arrow
           {:on-click #(reset! -ability ability)
            :data-tooltip
            (string/join "\n" [description (string/join "; " (map text/normalize-name traits))])}
           (text/normalize-name ability)]])]])))

(defn monster-turn-view [-encounter monster]
  (let [monster* (e/turn-effects-tick monster)]
    (if (zero? (:health monster*))
      [:div.content
       [:h3 (str (text/normalize-name (:name monster)) " has fainted from status effects!")]
       [next-turn-button -encounter]]
      (let [encounter @-encounter
            [ability target]
            (rand-nth
             (for [ability (e/get-usable-abilities encounter monster*)
                   :let [targets (e/get-possible-targets encounter monster* ability)
                         needs-target? (-> ability a/ability->details a/needs-target?)]
                   :when (seq targets)]
               (if needs-target?
                 [ability (rand-nth targets)]
                 [ability targets])))]
        [impacts-view -encounter monster* ability target]))))

(defn encounter-view [-encounter -stage]
  (let [encounter @-encounter]
    [:<>
     [:div.box
      [:div.columns
       [:div.column.is-5.has-text-centered
        [:h5 "Kobolds"]]
       [:div.column.is-2.has-text-centered
        [:h5 "Health"]]
       [:div.column.is-5.has-text-centered
        [:h5 "Monsters"]]]
      (for [i (range 4)
            :let [kobold (get-in encounter [:kobolds i])
                  monster (get-in encounter [:monsters i])]
            :when (or kobold monster)]
        ^{:key i}
        [:div.columns
         [:div.column.is-5
          (when kobold
            [:div.columns>div.column.is-8
             {:class
              (when (= :front (:row kobold))
                "is-offset-4")}
             [:button.button.is-info.is-fullwidth.is-outlined
              (:name kobold)]])]
         [:div.column.is-1.has-text-centered
          (when kobold
            [:p (str (:health kobold) " / " (get-in kobold [:stats :health]))])]
         [:div.column.is-1.has-text-centered
          (when monster
            [:p (str (:health monster) " / " (get-in monster [:stats :health]))])]
         [:div.column.is-5
          (when monster
            [:div.columns>div.column.is-8
             {:class
              (when (= :back (:row monster))
                "is-offset-4")}
             [:button.button.is-warning.is-fullwidth.is-outlined
              (:name monster)]])]])]
     [:div.columns
      [:div.column.is-narrow>div.box>div.content
       [:h3 (str "Round " (:round encounter))]
       [:p "Next up:"]
       [:ul
        (for [creature (:turn-order encounter)]
          ^{:key (:name creature)}
          [:li (text/normalize-name (:name creature))])]]
      [:div.column
       (if-let [creature (:turn encounter)]
         [:div.box>div.content
          [:h2 (str (text/normalize-name (:name creature)) "'s turn!")]
          [turn-view -encounter -stage (r/atom nil) (r/atom nil) creature]]
         (do
           (swap! -encounter e/next-round)
           [:<>]))]]]))
