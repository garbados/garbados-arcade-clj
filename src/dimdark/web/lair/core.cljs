(ns dimdark.web.lair.core 
  (:require
   [arcade.text :refer-macros [inline-slurp]]
   [clojure.string :as string]
   [dimdark.web.lair.crafting :refer [crafting-view]]
   [dimdark.web.lair.equipment :refer [inventory-view]]
   [dimdark.web.lair.kobolds :refer [kobolds-view]]
   [dimdark.web.lair.playground :refer [playground-view]]
   [reagent.core :as r]))

(def remarks
  (filter
   not-empty
   (string/split-lines
    (inline-slurp "resources/dimdark/remarks.txt"))))

(defn home-view [-game]
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

(defn side-view [-game]
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
         [:<>
          [:p "Relics:"]
          [:ul
           (for [relic relics]
             [:li (string/join " " (map string/capitalize (string/split (name relic) #"-")))])]])])))

(defn nav-view [-state]
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

(defn lair-view [-game -state]
  [:<>
   [nav-view -state]
   (case @-state
     :lair [home-view -game]
     :kobolds [kobolds-view -game (r/atom :drg)]
     :equipment [inventory-view -game]
     :crafting [crafting-view -game]
     :playground [playground-view -game (r/atom #{}) (r/atom nil) (r/atom :pre-selection)])])
