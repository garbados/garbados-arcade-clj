(ns dimdark.web.encounter 
  (:require [arcade.text :as text]
            [arcade.utils :refer [contains-v?]]
            [clojure.string :as string]
            [dimdark.abilities :as a]
            [dimdark.encounters :as e]
            [reagent.core :as r]))

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
       [:button.button.is-primary
        {:on-click #(e/next-turn @-encounter)}
        "Proceed"]])
    [:div.content
     [:h3 "Whiff!"]
     [:p (str (text/normalize-name (:name creature)) "'s use of " (text/normalize-name ability) " missed.")]
     [:button.button.is-primary.is-fullwidth
      {:on-click #(e/next-turn @-encounter)}
      "Proceed"]]))

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
      [:<>
       ]
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

(defn encounter-view [-encounter]
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
          (if (contains-v? (:kobolds encounter) creature)
            [kobold-turn-view -encounter creature (r/atom nil) (r/atom nil)]
            [monster-turn-view -encounter creature])]
         (do
           (swap! -encounter e/next-round)
           [:<>]))]]]))
