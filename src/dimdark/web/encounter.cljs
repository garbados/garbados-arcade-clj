(ns dimdark.web.encounter 
  (:require [arcade.text :as text]
            [arcade.utils :refer [indexOf]]
            [clojure.string :as string]
            [dimdark.abilities :as a]
            [reagent.core :as r]))

(defn do-ability [creature ability target]
  (let [{:keys [traits effects self-effects env-effects]} (a/ability->details ability)
        roll-magnitude
        (fn [target]
          (cond
            (contains? traits :hostile)
            (a/hostile-ability-hits? ability creature target)
            (contains? traits :friendly)
            (a/friendly-ability-hits? ability creature target)))]
    (if (seq? target)
      (let [magnitudes (->> (map #(vec [% (roll-magnitude %)]) target)
                            (filter (comp pos-int? second)))]
        (if (seq magnitudes)
          (reduce
           (fn [effects [target magnitude]])
           {}
           magnitudes)))
      (let [magnitude (roll-magnitude target)]
        (when (pos-int? magnitude)
          (cond-> {}
            (seq effects) (assoc-in [:effects target] (a/resolve-effects effects magnitude))
            (seq self-effects) (assoc :self-effects (a/resolve-effects self-effects magnitude))
            (seq env-effects) (assoc :env-effects (a/resolve-effects env-effects magnitude))))))))

(defn kobold-turn-view [-game -encounter kobold -ability -target]
  (let [ability @-ability
        target @-target]
   (cond
     (and ability target)
    ;;  FIXME when target is actually targets
     (if-let [impacts (do-ability kobold ability target)]
       (let [{:keys [effects self-effects env-effects]
              :or {effects {} self-effects {} env-effects {}}} impacts]
         [:<>
          [:h1 [:strong "Hit!"]]
          [:h5 "Effects"]
          [:ul
           (for [[effect amt] effects]
             ^{:key effect}
             [:li (str (text/normalize-name (:name target)) " <- " (text/normalize-name effect) ": " amt)])
           (for [[effect amt] self-effects]
             ^{:key effect}
             [:li (str (text/normalize-name (:name kobold)) " <- " (text/normalize-name effect) ": " amt)])
           (for [[effect amt] env-effects]
             ^{:key effect}
             [:li (str "Environment <- " (text/normalize-name effect) ": " amt)])]
          [:button.button.is-primary
           "Proceed"]])
       [:<>
        [:h3 "Whiff!"]
        [:p (str (text/normalize-name (:name kobold)) "'s use of " (text/normalize-name ability) " missed.")]
        [:button.button.is-primary
         {:on-click
          (fn [& _]
            (let [next-turn (first (:turn-order @-encounter))]
              (swap! -encounter update :turn-order (partial drop 1))
              (swap! -encounter assoc :turn next-turn)))}
         "Proceed"]])
     (and (some? ability)
          (nil? target))
     (let [{:keys [traits]} (ability a/ability->details)]
       (cond
         (contains? traits :self)
         [:<>
          [:p "This ability will only affect " (:name kobold)]
          [:div.columns
           [:div.column
            [:button.button.is-primary.is-fullwidth
             {:on-click #(reset! -target kobold)}
             "Proceed"]]
           [:div.column
            [:button.button.is-secondary.is-fullwidth
             {:on-click #(reset! -ability nil)}
             "Reset"]]]]
         (and (contains? traits :hostile)
              (contains? traits :area))
         [:<>
          [:p "This ability will affect:"]
          [:ul
           (for [name (map (comp text/normalize-name :name)
                           (:monsters @-encounter))]
             ^{:key name}
             [:li name])]
          [:div.columns
           [:div.column
            [:button.button.is-primary.is-fullwidth
             {:on-click #(reset! -target (:monsters @-encounter))}
             "Proceed"]]
           [:div.column
            [:button.button.is-secondary.is-fullwidth
             {:on-click #(reset! -ability nil)}
             "Reset"]]]]
         (and (contains? traits :friendly)
              (contains? traits :area))
         [:<>
          [:p "This ability will affect:"]
          [:ul
           (for [name (map (comp text/normalize-name :name)
                           (:kobolds @-encounter))]
             ^{:key name}
             [:li name])]
          [:div.columns
           [:div.column
            [:button.button.is-primary.is-fullwidth
             {:on-click #(reset! -target (:kobolds @-encounter))}
             "Proceed"]]
           [:div.column
            [:button.button.is-secondary.is-fullwidth
             {:on-click #(reset! -ability nil)}
             "Reset"]]]]
         :else
         ()
         #_(let [possible-targets (a/get-possible-targets ...)]))))))

(defn monster-turn-view [-game -encounter monster]
  )

(defn encounter-view [-game -encounter]
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
            [:div.columns>div.column.is-10
             {:class
              (when (= :front (:row kobold))
                "is-offset-2")}
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
            [:div.columns>div.column.is-10
             {:class
              (when (= :back (:row monster))
                "is-offset-2")}
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
       (let [creature (:turn encounter)]
         [:div.box>div.content
          [:h2 (str (text/normalize-name (:name creature)) "'s turn!")]
          (if (nat-int? (indexOf (:kobolds encounter) creature))
            [kobold-turn-view -game -encounter creature (r/atom nil) (r/atom nil)]
            [monster-turn-view -game -encounter creature])])]]]))
