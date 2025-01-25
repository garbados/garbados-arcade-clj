(ns dimdark.web.lair.kobolds 
  (:require
   [clojure.string :as string]
   [dimdark.abilities :as a]
   [dimdark.kobolds :as k]
   [dimdark.web.lair.equipment :refer [equipment-view]]))

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

(defn kobolds-view [-game -kobold]
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
