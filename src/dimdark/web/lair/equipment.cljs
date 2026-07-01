(ns dimdark.web.lair.equipment 
  (:require
   [clojure.string :as string]
   [dimdark.equipment :as eq]
   [dimdark.kobolds :as k]))

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

(defn inventory-view [-game]
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
