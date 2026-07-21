(ns ambition.bots
  "'autos' or 'automatic players'
   (as opposed to eyyy ayyy)
   which utilize various distinct strategies"
  (:require
   [ambition.core :as core]
   #?(:clj
      [clojure.math :as math]
      :cljs
      [cljs.math :as math])
   [clojure.spec.alpha :as spec]))

(spec/def ::bot-output
  (spec/nilable
   (spec/tuple
    ::core/action
    (spec/or :advance (spec/tuple ::core/coord ::core/coord)
             :pass true?
             :else ::core/coord))))

(defmacro ^:no-stest bot-spec [bot-name]
  `(spec/fdef ~bot-name
     :args (spec/cat :world ::core/world
                     :player ::core/player)
     :ret ::bot-output))

(defn random-action [action-options]
  (when (seq action-options)
    (let [[action options] (rand-nth (seq action-options))]
      (when (seq options)
        [action (rand-nth options)]))))

(spec/fdef random-action
  :args (spec/cat :action-options ::core/action-options)
  :ret ::bot-output)

(defn get-player-units [world player]
  (->> (:units world)
       (map first)
       (filter
        (fn [coord]
          (= player (core/get-claimant world coord))))))

(spec/fdef get-player-units
  :args (spec/cat :world ::core/world
                  :player ::core/player)
  :ret ::core/coords)

(defn despoiler
  "WORK TIL YOU CAN'T.
   BUY WHILE YOU CAN.
   GRIN FOR YOU MUST.
   DIE! DIE! DIE!"
  [world player]
  (let [{despoil-options :despoil
         :as action-options}
        (core/get-valid-actions world player)]
    (cond
      (seq despoil-options)
      [:despoil (rand-nth despoil-options)]
      :else
      (random-action action-options))))

(bot-spec despoiler)

(def board-center
  (let [center (/ (dec core/BOARD-SIZE) 2)
        floor (int (math/floor center))
        ceil (int (math/ceil center))]
    #{[floor floor]
      [floor ceil]
      [ceil floor]
      [ceil ceil]}))

(defn distance-to-center [coord]
  (->> board-center
       (map
        #(core/distance-between coord %))
       (apply min)))

(spec/fdef distance-to-center
  :args (spec/cat :coord ::core/coord)
  :ret nat-int?)

(defn overseer
  "OF COURSE OUR SUPERVISION IS NECESSARY.
   WHY, JUST THINK OF ALL THE TERRIBLE THINGS YOU MIGHT DO
   IF LEFT TO YOUR OWN DEVICES."
  [world player]
  (let [{advance-options :advance
         develop-options :develop
         muster-options  :muster
         :as action-options} (core/get-valid-actions world player)
        flanked-enemy-units
        (->> (:units world)
             (filter second)
             (map first)
             (filter
              (fn [coord]
                (and (not= player (core/get-claimant world coord))
                     (core/flanked? world coord))))
             set)
        stomp-options
        (and (seq advance-options)
             (->> advance-options
                  (filter
                   (fn [[_coord coord]]
                     (contains? flanked-enemy-units coord)))
                  seq))
        advance-to-center-options
        (and (seq advance-options)
             (->> advance-options
                  (filter
                   (fn [[_coord coord*]]
                     (contains? board-center coord*)))
                  seq))
        advance-toward-center-options
        (sort
         (fn [[_coord1 coord1*] [_coord2 coord2*]]
           (< (distance-to-center coord1*)
              (distance-to-center coord2*)))
         advance-options)]
    (cond
      (some? stomp-options)
      [:advance (rand-nth stomp-options)]
      (seq develop-options)
      [:develop (rand-nth develop-options)]
      (seq muster-options)
      [:muster (rand-nth muster-options)]
      (seq advance-to-center-options)
      [:advance (rand-nth advance-to-center-options)]
      (seq advance-toward-center-options)
      [:advance (rand-nth advance-toward-center-options)]
      :else
      (random-action action-options))))

(bot-spec overseer)

(defn technocrat
  "I'M SURE THAT WITH THE RIGHT PEOPLE IN PLACE --
   YOU KNOW, THE PEOPLE WHO KNOW ABOUT THIS SORT OF THING --
   THAT EVERYTHING WILL BE FINE."
  [world player]
  (let [{develop-options :develop
         muster-options :muster
         advance-options :advance
         :as action-options} (core/get-valid-actions world player)
        own-units (get-player-units world player)]
    (cond
      (and (zero? (count own-units))
           (seq muster-options))
      [:muster (rand-nth muster-options)]
      (seq develop-options)
      [:develop (rand-nth develop-options)]
      (seq advance-options)
      [:advance (rand-nth advance-options)]
      (seq muster-options)
      [:muster (rand-nth muster-options)]
      :else
      (random-action action-options))))

(bot-spec technocrat)

(defn expansionist
  "ONCE, WE SPANNED THE GLOBE.
   WHY NOT AGAIN?
   WHY NOT FOREVER?"
  [world player]
  (let [{develop-options :develop
         muster-options :muster
         advance-options :advance
         :as action-options} (core/get-valid-actions world player)
        own-units (get-player-units world player)
        undeveloped-options
        (filter
         (fn [coord]
           (zero? (core/get-infra world coord)))
         develop-options)
        frontier-options
        (filter
         (fn [[_coord coord]]
           (zero? (core/get-infra world coord)))
         advance-options)]
    (cond
      (seq undeveloped-options)
      [:develop (rand-nth undeveloped-options)]
      (seq frontier-options)
      [:advance (rand-nth frontier-options)]
      (and (zero? (count own-units))
           (seq muster-options))
      [:muster (rand-nth muster-options)]
      (seq develop-options)
      [:develop (rand-nth develop-options)]
      (seq advance-options)
      [:advance (rand-nth advance-options)]
      (seq muster-options)
      [:muster (rand-nth muster-options)]
      :else
      (random-action action-options))))

(bot-spec expansionist)

(defn discordian
  "FOR A PEOPLE THAT WORSHIP CONFUSION,
   THEY PROVE CURIOUSLY PREDICTABLE.
   IS WHAT KEEPS INQUIRY DRIVEN
   THE HUBRIS TO BELIEVE
   AN ANSWER EXISTS?"
  [world player]
  (random-action (core/get-valid-actions world player)))

(bot-spec discordian)

(def bots [despoiler
           overseer
           technocrat
           expansionist
           discordian])
