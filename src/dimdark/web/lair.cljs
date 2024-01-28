(ns dimdark.web.lair
  (:require [arcade.text :refer-macros [inline-slurp]]
            [dimdark.kobolds :as k]
            [reagent.core :as r]
            [clojure.string :as string]
            [dimdark.core :as d]
            [dimdark.equipment :as eq]
            [dimdark.abilities :as a]))

(def remarks
  (filter
   not-empty
   (string/split-lines
    (inline-slurp "resources/dimdark/remarks.txt"))))

(defn lair-view-menu [-state]
  (let [state @-state]
    [:div.columns
     (for [[name* state*] [["Lair" :lair]
                           ["Kobolds" :kobolds]
                           ["Equipment" :equipment]
                           ["Crafting" :crafting]
                           ["Playground" :playground]]]
       [:div.column
        [:button.button.is-info.is-fullwidth
         {:disabled (= state* state)
          :on-click #(reset! -state state*)}
         name*]])]))

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
            [:th (string/capitalize (name attribute))])]]
        [:tbody
         [:tr
          (for [value (map #(get-in kobold [:attributes %]) attributes)]
            [:th value])]]])
     [:h5 "Merits"]
     (let [merits [:scales :squish :stink :brat]]
       [:table.table
        [:thead
         [:tr
          (for [merit merits]
            [:th (string/capitalize (name merit))])]]
        [:tbody
         [:tr
          (for [merit merits]
            #_[[merit color] [[:scales :red]
                              [:squish :blue]
                              [:stink :green]
                              [:brat :purple]]]
            [:th (get-in kobold [:merits merit] 0)])]]])
     [:h5 "Equipment"]
     [:table.table
      [:tbody
       (for [slot [:weapon :armor :accessory]
             :let [obj (get-in kobold [:equipped slot])]
             :when (some? obj)]
         [:tr
          [:td.is-narrow [:em (string/capitalize (name slot))]]
          [:td
           [:div.level
            [:div.level-left
             [:div.level-item
              [:span (:name obj)]]]
            [:div.level-right
             [:div.level-item
              [:span
               (string/join
                "; "
                (for [[stat value] (eq/equipment->stats obj)
                      :when (pos-int? value)]
                  (str (string/capitalize (name stat)) ": " value)))]]]]]])]]]
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
  [:div.box>div.content
   [:h2 (str "Lair of " (:name @-game))]
   [:p [:em (rand-nth remarks)]]])

(defn lair-kobolds-view [_ -kobold]
  [:div.columns
   [:div.column.is-2
    (for [kobold-name (map first (sort-by first k/kobolds))]
      [:div.block
       [:button.button.is-link.is-fullwidth
        {:on-click #(reset! -kobold kobold-name)
         :disabled (= kobold-name @-kobold)}
        (string/capitalize (name kobold-name))]])]
   [:div.column.is-10
    (when @-kobold
      [kobold-view (get k/kobolds @-kobold)])]])

(defn lair-equipment-view [-game]
  (let [game-equipment
        (take 10 (repeatedly #(eq/gen-equipment-from-level-rarity (inc (rand-int 5)) (rand-nth eq/rarities))))
        #_(:equipment @-game)
        slot->equipment
        (reduce
         (fn [slots equipment]
           (update slots (:slot equipment) conj equipment))
         {:weapon [] :armor [] :accessory []}
         game-equipment)]
    [:div.box>div.content
     [:div.columns
      [:div.column.is-6
       [:h2 "Equipment"]]
      [:div.column.is-4
       {:style {:text-align :center}}
       [:h4 "Equippable by..."]]
      [:div.column.is-2
       {:style {:text-align :center}}
       [:h5 "Disenchant"]]]
     (for [slot [:weapon :armor :accessory]
           :let [equipment (seq (slot slot->equipment))]
           :when (some? equipment)]
       [:<>
        [:h3 (string/capitalize (name slot))]
        (for [equipment equipment]
          [:div.columns
           [:div.column.is-6
            [equipment-view equipment]]
           [:div.column.is-4
            [:div.level
             (for [kobold (vals (:kobolds @-game))]
               [:div.level-item
                [:button.button.is-info
                 {:disabled (not (k/equippable? kobold equipment))}
                 (:name kobold)]])]]
           [:div.column.is-2
            [:button.button.is-danger.is-fullwidth
             "🐲✨🗑️"]]])])]))

(defn lair-crafting-view [-game]
  [:h3 "UNIMPLEMENTED"])

(defn lair-playground-view [-game]
  [:h3 "UNIMPLEMENTED"])

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
     :playground [lair-playground-view -game])])
