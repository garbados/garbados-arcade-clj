(ns dimdark.web.lair.playground 
  (:require
   [dimdark.games :as g]
   [dimdark.kobolds :as k]
   [dimdark.web.encounter :as encounter]))

(defn playground-view [-game -team -encounter -stage]
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
                     encounter (g/init-playground-encounter kobolds1 kobolds2)]
                 (reset! -encounter encounter))}
             "Begin!"]]
           [:div.column
            [:button.button.is-warning.is-fullwidth
             {:on-click #(reset! -team #{})}
             "Reset!"]]])])]))
