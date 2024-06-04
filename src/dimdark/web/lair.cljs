(ns dimdark.web.lair
  (:require [arcade.text :refer-macros [inline-slurp]]
            [clojure.string :as string]
            [dimdark.abilities :as a]
            [dimdark.equipment :as eq]
            [dimdark.games :as dg]
            [dimdark.kobolds :as k]
            [dimdark.web.encounter :as encounter]
            [reagent.core :as r]))

(def remarks
  (filter
   not-empty
   (string/split-lines
    (inline-slurp "resources/dimdark/remarks.txt"))))

(defn lair-view-menu [-state]
  [:div.columns>div.column.is-6.is-offset-3
   (let [state @-state]
     [:div.columns
      (for [[name* state*] [["Lair" :lair]
                            ["Kobolds" :kobolds]
                            ["Equipment" :equipment]
                            ["Crafting" :crafting]
                            ["Playground" :playground]]]
        ^{:key name*}
        [:div.column
         [:button.button.is-info.is-fullwidth
          {:disabled (= state* state)
           :on-click #(reset! -state state*)}
          name*]])])])

(defn stats-table [stats]
  [:table.table
   [:tbody
    (let [printable-stats
          (reduce
           (fn [stats [stat value]]
             (if (map? value)
               (reduce
                (fn [stats [element value]]
                  (assoc stats
                         (string/join ": " (map (comp string/capitalize name) [stat element]))
                         value))
                stats
                value)
               (assoc stats (string/capitalize (name stat)) value)))
           {}
           stats)]
      (for [stat ["Health" "Attack" "Defense" "Armor" "Initiative" "Fortune"
                  "Aptitude"
                  "Aptitudes: Fire"
                  "Aptitudes: Frost"
                  "Aptitudes: Poison"
                  "Aptitudes: Mental"
                  "Resistance"
                  "Resistances: Fire"
                  "Resistances: Frost"
                  "Resistances: Poison"
                  "Resistances: Mental"]
            :let [value (get printable-stats stat)]
            :when (pos-int? value)]
        ^{:key stat}
        [:tr
         [:td stat]
         [:td value]]))]])

(defn equipment-view [equipment]
  [:div.level
   [:div.level-left
    [:div.level-item
     [:span [:strong (:name equipment)]]]
    [:div.level-item
     [:span [:em (string/join ", "
                              [(name (:type equipment))
                               (str "lvl " (:level equipment))])]]]]
   [:div.level-right
    [:div.level-item
     [:span
      (string/join
       "; "
       (for [[stat value] (eq/equipment->stats equipment)
             :when (pos-int? value)]
         (str (string/capitalize (name stat)) ": " value)))]]]])

(defn kobold-view [kobold]
  [:div.box>div.content
   [:div.level
    [:div.level-left
     [:div.level-item
      [:h3 (:name kobold)]]
     [:div.level-item
      [:h5 (name (:class kobold))]]]
    [:div.level-right
     [:div.level-item
      [:h5 (:row kobold)]]]]
   [:div.columns
    [:div.column.is-8
     [:h5 "Attributes"]
     (let [attributes [:prowess :alacrity :vigor :spirit :focus :luck]]
       [:table.table
        [:thead
         [:tr
          (for [attribute attributes]
            ^{:key attribute}
            [:th (string/capitalize (name attribute))])]]
        [:tbody
         [:tr
          (for [attribute attributes
                :let [value (get-in kobold [:attributes attribute])]]
            ^{:key attribute}
            [:th value])]]])
     [:h5 "Merits"]
     (let [merits [:scales :squish :stink :brat]]
       [:table.table
        [:thead
         [:tr
          (for [merit merits]
            ^{:key merit}
            [:th (string/capitalize (name merit))])]]
        [:tbody
         [:tr
          (for [merit merits]
            #_[[merit color] [[:scales :red]
                              [:squish :blue]
                              [:stink :green]
                              [:brat :purple]]]
            ^{:key merit}
            [:th (get-in kobold [:merits merit] 0)])]]])
     [:h5 "Equipment"]
     [:table.table
      [:tbody
       (for [slot [:weapon :armor :accessory]
             :let [equipped (get-in kobold [:equipped slot])]
             :when (some? equipped)]
         ^{:key slot}
         [:tr
          [:td.is-narrow [:em (string/capitalize (name slot))]]
          [:td
           [equipment-view equipped]]])]]]
    [:div.column.is-4
     (let [stats (k/kobold->stats kobold)]
       [:<>
        [:h5 "Stats"]
        [stats-table stats]])]]
   [:<>
    [:h5 "Abilities"]
    (for [ability (:abilities kobold)
          :let [details (a/ability->details ability)]
          :when (some? details)]
      ^{:key ability}
      [:div.level
       [:div.level-left
        [:div.level-item
         [:p
          [:span [:strong (string/join " " (map string/capitalize (string/split (name ability) "-")))]]
          ": "
          (:description details)]]]
       [:div.level-right
        [:div.level-item
         [:p (string/join ", " (map (comp (partial string/join " ")
                                          #(map string/capitalize %)
                                          #(string/split % "-")
                                          name)
                                    (:traits details)))]]]])]])

(defn lair-home-view [-game]
  [:div.columns>div.column.is-6.is-offset-3>div.box>div.content
   [:h2 (str "Lair of " (:name @-game))]
   [:p [:em (rand-nth remarks)]]
   [:p "What will you do now?"]
   [:p
    [:button.button.is-primary.is-fullwidth
     "Quests"]]
   [:p
    [:button.button.is-secondary.is-fullwidth
     "Delving"]]])

(defn lair-kobolds-view [-game -kobold]
  (let [kobolds (:kobolds @-game)]
    [:div.columns>div.column.is-10.is-offset-1
     [:div.columns
      [:div.column.is-2
       (for [kobold-name (map first (sort-by first kobolds))]
         ^{:key kobold-name}
         [:div.block
          [:button.button.is-link.is-fullwidth
           {:on-click #(reset! -kobold kobold-name)
            :disabled (= kobold-name @-kobold)}
           (string/capitalize (name kobold-name))]])]
      [:div.column.is-10
       (when @-kobold
         [kobold-view (get kobolds @-kobold)])]]]))

(defn equipping-button [kobold equipment equipped -game]
  (let [kobold-name (:name kobold)
        equippable (k/equippable? kobold equipment)
        tooltip? (and equippable (some? equipped))]
    [:button.button.is-info.is-fullwidth
     {:disabled (not equippable)
      :class (if tooltip? "has-tooltip-arrow" "is-outlined")
      :on-click
      (when equippable
        #(do
           (swap! -game assoc-in [:kobolds kobold-name :equipped (:slot equipment)] equipment)
           (swap! -game update :equipment conj equipped)))
      :data-tooltip
      (when tooltip?
        (string/join
         "; "
         (for [[stat value] (eq/equipment->stats equipped)
               :when (pos-int? value)]
           (str (string/capitalize (name stat)) ": " value))))}
     (:name kobold)]))

(defn lair-equipment-view [-game]
  (let [slot->equipment
        (reduce
         (fn [slots equipment]
           (update slots (:slot equipment) conj equipment))
         {:weapon [] :armor [] :accessory []}
         (:equipment @-game))]
    [:div.box>div.content
     [:div.columns
      [:div.column
       [:h2 "Equipment"]]
      [:div.column.is-4
       {:style {:text-align :center}}
       [:h4 "Equippable by..."]]
      [:div.column.is-1
       {:style {:text-align :center}}
       [:h5 "Disenchant"]]]
     (for [slot [:weapon :armor :accessory]
           :let [equipment (seq (slot slot->equipment))]
           :when (some? equipment)]
       ^{:key slot}
       [:<>
        [:h3 (string/capitalize (name slot))]
        (for [equipment equipment]
          ^{:key equipment}
          [:div.columns
           [:div.column
            [equipment-view equipment]]
           [:div.column.is-4
            [:div.columns
             (for [[kobold-name kobold] (:kobolds @-game)
                   :let [equipped (get-in kobold [:equipped slot])]]
               ^{:key kobold-name}
               [:div.column
                [equipping-button kobold equipment equipped -game]])]]
           [:div.column.is-1
            [:button.button.is-danger.is-fullwidth.is-outlined
             "🐲✨🗑️"]]])])]))

(defn lair-crafting-view [-game]
  [:h3 "UNIMPLEMENTED"])

(defn lair-playground-view [-game -team -encounter -stage]
  (if (some? @-encounter)
    [:div.columns>div.column.is-10.is-offset-1
     [encounter/encounter-view -encounter -stage]]
    [:div.columns>div.column.is-6.is-offset-3>div.box>div.content
     [:h3 "Playground"]
     [:p "The kobolds want to do some rough-housing. Who do you pick for your team?"]
     (let [team @-team]
       [:<>
        [:div.columns
         (for [kobold-name (keys (:kobolds @-game))
               :let [on-team? (contains? team kobold-name)]]
           ^{:key kobold-name}
           [:div.column
            [:button.button.is-outlined.is-fullwidth
             {:class (if on-team?
                       :is-success
                       :is-info)
              :disabled (= 3 (count team))
              :on-click #(if on-team?
                           (swap! -team disj kobold-name)
                           (swap! -team conj kobold-name))}
             (name kobold-name)]])]
        (when (= 3 (count team))
          [:div.columns
           [:div.column
            [:button.button.is-success.is-fullwidth
             {:on-click
              #(let [[kobolds1 kobolds2]
                     (reduce
                      (fn [[kobolds1 kobolds2] kobold-name]
                        (let [kobold (k/kobold->creature (get-in @-game [:kobolds kobold-name]))]
                          (if (contains? team kobold-name)
                            [(conj kobolds1 kobold) kobolds2]
                            [kobolds1 (conj kobolds2 kobold)])))
                      [[] []]
                      k/kobold-names)
                     encounter (dg/init-playground-encounter kobolds1 kobolds2)]
                 (reset! -encounter encounter))}
             "Begin!"]]
           [:div.column
            [:button.button.is-warning.is-fullwidth
             {:on-click #(reset! -team #{})}
             "Reset!"]]])])]))

(defn lair-side-view [-game]
   (let [{:keys [essence experience max-depth relics]} @-game]
     (when (some pos-int? [experience essence max-depth])
       [:div.box>div.content
        (when (pos-int? experience)
          [:p (str "Experience: " experience)])
        (when (pos-int? essence)
          [:p (str "Essence: " essence)])
        (when (pos-int? max-depth)
          [:p (str "Delve Depth: " max-depth)])
        (when (seq relics)
          [:p "Relics:"]
          [:ul
           (for [relic relics]
             [:li (string/join " " (map string/capitalize (string/split (name relic) #"-")))])])])))

(defn lair-view [-game -state]
  [:<>
   [lair-view-menu -state]
   (case @-state
     :lair [lair-home-view -game]
     :kobolds [lair-kobolds-view -game (r/atom :drg)]
     :equipment [lair-equipment-view -game]
     :crafting [lair-crafting-view -game]
     :playground [lair-playground-view -game (r/atom #{}) (r/atom nil) (r/atom :pre-selection)])])
