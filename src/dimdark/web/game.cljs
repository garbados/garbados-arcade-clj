(ns dimdark.web.game
  (:require [arcade.text :refer-macros [inline-slurp]]
            [dimdark.kobolds :as k]
            [reagent.core :as r]
            [clojure.string :as string]
            [dimdark.core :as d]
            [dimdark.equipment :as eq]
            [dimdark.abilities :as a]))

(defn color-text [color x]
  [:span {:style {:color color}} x])

(def remarks
  (filter
   not-empty
   (string/split-lines
    (inline-slurp "resources/dimdark/remarks.txt"))))

(defn in-lair? [game]
  (and (nil? (:escapade game))
       (nil? (:adventure game))))

(defn on-escapade? [game]
  (some? (:escapade game)))

(defn on-adventure? [game]
  (some? (:adventure game)))

(defn lair-view-menu [-state]
  [:<>
   [:div.level
    [:button.button.is-fullwidth
     {:disabled (= :lair @-state)
      :on-click #(reset! -state :lair)}
     "Lair"]]
   [:div.level
    [:button.button.is-fullwidth
     {:disabled (= :kobolds @-state)
      :on-click #(reset! -state :kobolds)}
     "Kobolds"]]
   [:div.level
    [:button.button.is-fullwidth
     {:disabled (= :equipment @-state)
      :on-click #(reset! -state :equipment)}
     "Equipment"]]
   [:div.level
    [:button.button.is-fullwidth
     {:disabled (= :crafting @-state)
      :on-click #(reset! -state :crafting)}
     "Crafting"]]])

(defn kobold-view [kobold]
  [:div.box>div.content
   [:div.level
    [:div.level-left
     [:div.level-item
      [:h3 (:name kobold)]]]
    [:div.level-right
     [:div.level-item
      [:h4 (name (:class kobold))]]]]
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
            (k/kobold->stats kobold))]
       [:<>
        [:h5 "Stats"]
        [:table.table
         [:tbody
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
             [:td value]])]]])]]
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
   [:h1 (str "Lair of " (:name @-game))]
   [:p [:em (rand-nth remarks)]]])

(defn lair-kobolds-view [-game -kobold]
  [:<>
   [:div.columns
    (for [kobold-name (map first (sort-by first k/kobolds))]
      [:div.column.is-2
       [:button.button.is-info.is-fullwidth
        {:on-click #(reset! -kobold kobold-name)
         :disabled (= kobold-name @-kobold)}
        (string/capitalize (name kobold-name))]])]
   (when @-kobold
     [kobold-view (get k/kobolds @-kobold)])])

(defn lair-view [-game -state]
  [:div.columns
   [:div.column.is-2
    [lair-view-menu -state]]
   [:div.column.is-8
    (case @-state
      :lair [lair-home-view -game]
      :kobolds [lair-kobolds-view -game (r/atom :drg)])]
   ;; this needs to get templated and everything
   [:div.column.is-2
    [:div.box>div.content
     [:p "Experience: 20"]
     [:p "Relics:"]
     [:ul
      [:li "Despot's Crown"]
      [:li "Ring of Binding"]]]]])

(defn game-view [-game]
  (cond
    (on-escapade? @-game)
    'todo
    (on-adventure? @-game)
    'todo
    :else
    [lair-view -game (r/atom :kobolds)]))
