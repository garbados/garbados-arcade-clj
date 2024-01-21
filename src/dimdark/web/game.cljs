(ns dimdark.web.game
  (:require [arcade.text :refer-macros [inline-slurp]]
            [dimdark.kobolds :as k]
            [reagent.core :as r]
            [clojure.string :as string]))

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
      [:h5 (name (:class kobold))]]]]
   [:p "Attributes"]
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
   [:p "Merits"]
   (let [merits [:scales :squish :stink :brat]]
     [:table.table
      [:thead
       [:tr
        (for [merit merits]
          [:th (string/capitalize (name merit))])]]
      [:tbody
       [:tr
        (for [merit merits]
          [:th (k/kobold-stat merit kobold)])]]])
   (for [stat [:aptitude :resistance]]
     (let [elements [:fire :frost :poison :mental]
           element->stat #(keyword (str (name %) "-" (name stat)))]
       [:<>
        [:p (str (string/capitalize (name stat)) "s")]
        [:table.table
         [:thead
          [:tr
           (for [element elements]
             [:th (string/capitalize (name element))])]]
         [:tbody
          [:tr
           (for [element elements]
             [:th (k/kobold-stat (element->stat element) kobold)])]]]]))
   (for [slot [:weapon :armor :accessory]
         :let [obj (get-in kobold [:equipped slot])]
         :when (some? obj)]
     [:div.level
      [:div.level-left
       [:div.level-item
        [:span [:em (string/capitalize (name slot))]]
        [:span (:name obj)]]]
      [:div.level-right
       [:div.level-item
        [:span (string/join ", " (map name (:modifiers obj)))]]]])])

(defn lair-home-view [-game] 
  [:div.column>div.box>div.content
   [:h1 (str "Lair of " (:name @-game))]
   [:p [:em (rand-nth remarks)]]])

(defn lair-kobolds-view [-game]
  [:div.column>div.box>div.content
   (for [kobold (map second (sort-by first k/kobolds))]
     [kobold-view kobold])])

(defn lair-view [-game -state]
  [:div.columns
   [:div.column.is-2
    [lair-view-menu -state]]
   [:div.column.is-10
    (case @-state
      :lair [lair-home-view -game]
      :kobolds [lair-kobolds-view -game])]])

(defn game-view [-game]
  (cond
    (on-escapade? @-game)
    'todo
    (on-adventure? @-game)
    'todo
    :else
    [lair-view -game (r/atom :kobolds)]))
