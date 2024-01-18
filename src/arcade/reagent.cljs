(ns arcade.reagent)

(defn prompt-text [value on-submit]
  [:input.input
   {:type "text"
    :value @value
    :on-change #(reset! value (-> % .-target .-value))
    :on-key-down
    (fn [e]
      (when (= 13 (.-which e))
        (on-submit @value)))}])

(defn prompt-int [value maximum]
  [:input.input
   {:type "number"
    :min 0
    :max maximum
    :value @value
    :on-change #(reset! value (-> % .-target .-value))}])
