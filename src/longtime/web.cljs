(ns longtime.web
  (:require ["pouchdb" :as pouchdb]
            [arcade.db :as db :refer [delete-doc fetch-doc save-doc]]
            [arcade.reagent :refer [prompt-int prompt-text]]
            [clojure.string :as string]
            [longtime.contact-text :as contact-text]
            [longtime.core :as core]
            [longtime.dream :as dream]
            [longtime.event :as event]
            [longtime.meta-text :as meta-text]
            [longtime.moment :as moment]
            [longtime.project :as project]
            [longtime.remark :as remark]
            [arcade.text :as text]
            [reagent.core :as r]
            [reagent.dom :as rd]))

(defonce db (new pouchdb "longtime"))
(defonce state (r/atom :loading))
(defonce games (r/atom []))
(defonce game (r/atom nil))
(defonce herd (r/atom nil))
(defonce gamestate (r/atom :intro))
(defonce monthstep (r/atom :event))
(defonce dream (r/atom nil))
(defonce extra? (r/atom false))
(defonce extrachoice (r/atom nil))

(def dismantle-infra
  {:name "Dismantle infrastructue"
   :uses [:craftwork]
   :filter-fn
   (fn [herd]
     (seq (:infra (core/current-location herd))))
   :effect
   (fn [herd location]
     (core/assoc-location herd (disj location :infra @extrachoice)))})

(def web-projects
  (conj project/projects dismantle-infra))

(defn- find-games []
  (.then (db/list-docs db)
         #(reset! games %)))

(defn- save-game []
  (save-doc db @game
            {:herd @herd
             :monthstep @monthstep}))

(defn- load-game [name]
  (reset! game name)
  (.then (fetch-doc db name)
         (fn [doc]
           (reset! herd (:herd doc))
           (reset! state :playing)
           (reset! gamestate :playing)
           (reset! monthstep (:monthstep doc)))))

(defn- new-game [name]
  (reset! herd (core/gen-herd :spirit name))
  (reset! game name)
  (save-game)
  (find-games)
  (reset! state :playing))

(defn- setup []
  (.then (find-games)
         #(let [n (count @games)]
            (cond
              (zero? n) (reset! state :new-game)
              (= 1 n)   (load-game (first @games))
              :else     (reset! state :list-games)))))

(defn- delete-game [name]
  (-> (delete-doc db name)
      (.then #(when (= @game name)
                (reset! game nil)
                (reset! herd nil)
                (reset! gamestate :intro)
                (reset! monthstep :event)))
      (.then find-games)
      (.then #(if (zero? (count @games))
                (reset! state :new-game)
                (reset! state :list-games)))))

(defn- init-new-game []
  (let [value (r/atom "")]
    [:div.container
     [:div.box
      [:div.content
       [:h3 meta-text/intro-text]]]
     [:div.field
      [:label.label "What shall the herd call you?"]
      [:div.control
       [prompt-text value new-game]]]]))

(defn- list-games []
  [:div.container>div.box>div.content
   [:p "Select a game to play:"]
   [:ul
    (for [game @games]
      ^{:key game}
      [:div.block
       [:div.columns
        [:div.column
         [:button.button.is-link.is-fullwidth
          {:on-click #(load-game game)}
          (str game)]]
        [:div.column.is-narrow
         [:button.button.is-danger
          {:on-click #(delete-game game)}
          "X"]]]])]])

(defn- loading []
  (setup)
  [:div.container>div.box>div.content
   [:h3 "Loading..."]])

(defn- credits []
  [:div.container>div.box>div.content
   [:h3 "Credits"]
   (let [lines (string/split-lines meta-text/credits-description)]
     (for [i (range (count lines))
           :let [line (nth lines i)]]
       ^{:key i}
       [:p line]))])

(defn- navbar []
  [:div.level
   [:div.level-left
    [:div.level-item
     [:h1.title "The Longtime"]]
    [:div.level-item
     [:button.button.is-primary
      {:on-click #(reset! state :new-game)}
      "New Game"]]
    (when (pos-int? (count @games))
      [:div.level-item
       [:button.button.is-link
        {:on-click #(reset! state :list-games)}
        "List Games"]])
    (when (some? @herd)
      [:div.level-item
       [:button.button.is-info
        {:on-click #(do (reset! state :playing)
                        (reset! gamestate :playing))}
        "Play Game"]])
    (when (some? @herd)
      (for [state* [:intro :projects :individuals :path]]
        ^{:key state*}
        [:div.level-item
         [:button.button.is-info
          {:on-click #(do (reset! state :playing)
                          (reset! gamestate state*))}
          (text/normalize-name state*)]]))]
   [:div.level-right
    [:div.level-item
     [:button.button.is-info.is-light
      {:on-click #(reset! state :credits)}
      "Credits"]]
    [:div.level-item
     [:a.button.is-info.is-light
      {:href "./index.html"}
      "Arcade"]]
    [:div.level-item
     [:p.subtitle
      [:strong "A game by "
       [:a {:href "https://www.patreon.com/garbados"
            :target "_blank"}
        "DFB"]]]]]])

(defn- print-location [location]
  (let [strings
        (filter
         some?
         [(when (pos-int? (:power location))
            (str "Power: " (:power location)))
          (when (some? (:flora location))
            (str "Flora: " (:flora location)))
          (when (some? (:depleted? location))
            (str "Depleted? " (:depleted? location)))
          (when (= :plains (:terrain location))
            (str "Nutrients: "
                 (string/join
                  ", "
                  (map (fn [nutrient]
                         (string/join
                          " "
                          [(text/normalize-name nutrient)
                           (get location nutrient)]))
                       [:n :k :p]))))
          (when (some? (:crop location))
            (str "Crop: " (text/normalize-name (:crop location))))
          (when (some? (:ready? location))
            (str "Ready? " (:ready? location)))
          (when (some? (:wild? location))
            (str "Wild? " (:wild? location)))
          (when-let [infra (seq (:infra location))]
            (str "Infra: " (string/join ", " (sort (map text/normalize-name infra)))))
          (when-let [stores (seq (filter (comp pos-int? second) (:stores location)))]
            (str "Stores: "
                 (string/join
                  ", "
                  (for [[resource amount] (sort-by first stores)]
                    (str amount " " (text/normalize-name resource))))))])]
    [:li
     [:span (str "Location: " (:name location))]
     [:ul
      (for [i (range (count strings))
            :let [s (nth strings i)]]
        ^{:key i} [:li s])]]))

(defn- print-location-brief [location]
  (let [infra (seq (map text/normalize-name (:infra location)))]
    [:li (str (:name location) (if infra (str ": " (string/join ", " infra)) ""))]))

(defn- print-herd []
  (let [{:keys [individuals syndicates spirit month
                hunger sickness stores path]
         :as herd*}
        @herd
        population (count individuals)]
    [:div.box>div.content
     [:h3 (:name herd*)]
     [:h5 (str "Shepherded by the " spirit)]
     [:ul
      [:li (let [year (inc (int (/ month 12)))
                 month* (inc (rem month 12))
                 season (core/int->season (core/get-season herd*))]
             (str "Year " year ", month " month* " (" season ")"))]
      [:li (str "Population: " population)]
      [:li (str "Syndicates: " (string/join ", " (sort (map core/syndicate-name syndicates))))]
      [:li (let [need (core/calc-food-need population)
                 ok? (core/herd-has-nutrition? herd* need)
                 ! (if ok? "" "!")]
             (str "Hunger: " hunger " (-" need ! ")"))]
      [:li (let [need (core/calc-meds-need population)
                 ok? (>= (get-in herd* [:stores :poultices] 0) need)
                 value (str (int (* (/ sickness population) 100)) "%")
                 ! (if ok? "" "!")]
             (str "Sickness: " value " (-" need ! ")"))]
      [:li (let [fulfillments (map :fulfillment individuals)
                 average (/ (reduce + 0 fulfillments) population)
                 minimum (reduce min fulfillments)
                 maximum (reduce max fulfillments)]
             (str "Fulfillment: "
                  "avg " (int average) "%; "
                  "min " minimum "%; "
                  "max " maximum "%"))]
      [:li
       [:span "Skills:"]
       (let [skills (->> core/skills
                         (map
                          (fn [skill]
                            [skill (core/collective-skill herd* skill)]))
                         sort)]
         [:table.table
          [:thead
           [:tr
            (for [[skill _] skills]
              ^{:key skill} [:th (text/normalize-name skill)])]]
          [:tbody
           [:tr
            (for [[skill amount] skills]
              ^{:key skill} [:td amount])]]])]
      [:li
       [:span "Stores:"]
       (let [stores* (sort (seq stores))]
         [:table.table
          [:thead
           [:tr
            (for [[resource _] stores*]
              ^{:key resource} [:th (text/normalize-name resource)])]]
          [:tbody
           [:tr
            (for [[resource amount] stores*]
              ^{:key resource} [:td amount])]]])]
      (when-let [contacts (seq (:contacts herd*))]
        [:li [:p (str "Contacts: " (string/join ", " (map text/normalize-name contacts)))]])
      (when-let [space (seq (:space herd*))]
        [:li [:p (str "Space: " (string/join ", " (map text/normalize-name space)))]])
      [print-location (core/current-location herd*)]
      [:li
       [:span "Next Stage:"
        [:ul
         (for [location (second path)]
           ^{:key (:terrain location)} [print-location-brief location])]]]]]))

(defn- intro []
  [:div
   [:div.box>div.content
    [:h3 "Welcome to " [:em "The Longtime"]]
    (let [lines (string/split-lines (meta-text/tutorial-text @herd))]
      (for [i (range (count lines))
            :let [line (nth lines i)]]
        ^{:key i} [:p line]))]
   [:button.button.is-fullwidth.is-primary
    {:on-click #(reset! gamestate :playing)}
    "Then let us begin!"]])

(defn- explain-map [map*]
  [:ul
   (for [[x y] map*]
     ^{:key x} [:li (str (text/normalize-name x) ": " (text/normalize-name y))])])

(defn- explain-filter [{:keys [stores skills terrain season infra space contacts power]}]
  [:ul
   (when stores
     [:li
      [:span "Required resources:"]
      [explain-map stores]])
   (when skills
     [:li
      [:span "Required skills:"]
      [explain-map skills]])
   (when terrain
     [:li (str "Only in "
               (text/normalize-name terrain))])
   (when season
     [:li (str "Only during "
               (core/int->season season))])
   (when infra
     [:li (str "Needs "
               (cond
                 (seq? infra) (string/join ", " (map text/normalize-name infra))
                 :else        (text/normalize-name infra)))])
   (when space
     [:li (str "Needs in orbit: "
               (cond
                 (seq? space) (string/join ", " (map text/normalize-name space))
                 :else        (text/normalize-name space)))])
   (when contacts
     [:li (str "Requires relations with "
               (cond
                 (seq? contacts) (string/join ", " (map text/normalize-name contacts))
                 :else           (text/normalize-name contacts)))])
   (when power
     [:li (str "Requires " power " energy")])])

(defn- projects []
  [:div.box>div.content
   [:h3 "Projects"]
   [:p meta-text/projects-description]
   [:hr]
   (for [project (sort-by :name project/projects)]
     ^{:key (:name project)}
     [:div.box
      [:p [:strong (:name project)]]
      [:p
       [:span (:description project)]
       [:em (str " " (:detail project))]]
      (when-let [uses (seq (:uses project))]
        [:p (str "Uses " (string/join ", " (map text/normalize-name uses)) ".")])
      (when-let [filter* (:filter project)]
        [explain-filter filter*])])])

(defn- individuals []
  [:div.box>div.content
   [:h3 "Individuals"]
   [:p meta-text/individuals-description]
   [:hr]
   (let [herd* @herd]
     (for [individual (sort-by :name (:individuals herd*))
           :let [smiley (cond
                          (> (:fulfillment individual) (* core/max-fulfillment (/ 2 3))) "😄"
                          (> (:fulfillment individual) (* core/max-fulfillment (/ 1 2))) "😀"
                          (> (:fulfillment individual) (* core/max-fulfillment (/ 1 3))) "🙁"
                          :else "😢")]]
       ^{:key (str individual)}
       [:div.box
        [:p [:strong (str (:name individual) ", "
                          (core/get-age herd* individual) ", "
                          smiley)]]
        [:ul
         (when-let [passions (seq (:passions individual))]
           [:li (str "Passions: " (string/join ", " (map text/normalize-name passions)))])
         (when-let [traits (seq (:traits individual))]
           [:li (str "Traits: " (string/join ", " (map text/normalize-name traits)))])
         (when-let [skills (sort-by first (seq (:skills individual)))]
           [:li
            [:span (str "Skills:")]
            [:table.table
             [:thead
              [:tr
               (for [[skill _] skills]
                 ^{:key skill} [:th (text/normalize-name skill)])]]
             [:tbody
              [:tr
               (for [[skill amount] skills]
                 ^{:key skill} [:td amount])]]]])]]))])

(defn- path []
  [:div.box>div.content
   [:h3 "The Path"]
   [:p meta-text/path-description]
   [:hr]
   (let [herd* @herd]
     (for [i (range (count (:path herd*)))
           :let [stage (-> herd* :path (nth i))]]
       ^{:key i}
       [:div.box
        [:p [:strong (cond
                       (zero? i) (str "Stage " (inc i) " (current)")
                       (= 1 i)   (str "Stage " (inc i) " (next)")
                       :else     (str "Stage " (inc i)))]]
        [:ul
         (for [location stage]
           ^{:key (:terrain location)}
           (print-location location))]]))])

(defn- handle-steppe [herd* location]
  (let [remarks (remark/gen-remarks herd*)]
    [:div.box>div.content
     [:h3 "Entering " (:name location) "..."]
     [:p remarks]
     [:div
      [:p "The herd rushes unfettered across the steppe."]
      [:button.button.is-fullwidth.is-primary
       {:on-click #(do (reset! herd herd*)
                       (reset! monthstep :next))}
       "Proceed to select next location"]]]))

(defn- handle-new-month [herd* location]
  (let [herd* (-> herd* core/begin-month core/consolidate-stores)
        event (when (zero? (rand-int 3))
                (event/pick-event herd*))
        herd* (if-let [[name* _text-fn effect] event]
                (assoc (effect) :event name*)
                herd*)
        remarks (string/join " " [(remark/gen-remarks herd*)
                                  (moment/gen-moments herd*)])]
    [:div.box>div.content
     [:h3 "Entering " (:name location) "..."]
     [:p remarks]
     (when-let [new-adults (seq (:new-adults herd*))]
       (let [plural? (< 1 (count new-adults))
             verb (if plural?
                    "minots have"
                    "minot has")
             s? (if plural?
                  "s"
                  "")]
         [:p (str (count new-adults) " " verb " come in from their journey" s? ": "
                  (string/join ", " (map :name new-adults)))]))
     (when-let [new-dead (seq (:new-dead herd*))]
       (let [plural? (> 1 (count new-dead))
             verb (if plural?
                    "minots have"
                    "minot has")]
         [:p   (str (count new-dead) " " verb " returned to soil: "
                    (string/join ", " (map :name new-dead)))]))
     (when event
       [:<>
        [:h5 (-> event first text/normalize-name)]
        [:p ((second event))]])
     [:button.button.is-fullwidth.is-primary
      {:on-click #(do (reset! herd herd*)
                      (reset! monthstep :projects))}
      "Proceed to select projects"]]))

(defn- handle-event []
  (save-game)
  (reset! dream nil)
  (let [herd*    @herd
        location (core/current-location herd*)
        steppe?  (= :steppe (:terrain location))]
    (if steppe?
      [handle-steppe herd* location]
      [handle-new-month herd* location])))

(defn- enact-project [proj]
  (if (= (:name proj) (:name dismantle-infra))
    (reset! extra? true)
    (let [herd* (-> (project/do-project @herd proj)
                    (update :projects conj (:name proj)))]
      (reset! herd herd*)
      (when (= 3 (count (:projects herd*)))
        (reset! monthstep :dream)))))

(defn- handle-projects []
  (let [herd* @herd
        i (inc (count (:projects herd*)))
        candidates
        (filter (partial project/can-enact? herd*)
                web-projects)]
    (if @extra?
      [:div.box>div.content
       [:h3 (str "Select what to dismantle:")]
       (for [infra (:infra (core/current-location herd*))]
         ^{:key infra}
         [:p
          [:button.button.is-info.is-fullwidth
           {:on-click #(do (reset! extrachoice infra)
                           (enact-project dismantle-infra)
                           (reset! extra? false)
                           (reset! extrachoice nil))}
           (text/normalize-name infra)]])]
      [:div.box>div.content
       [:h3 (str "Select project " i " of 3:")]
       (for [{:keys [name] :as proj} (sort-by :name candidates)]
         ^{:key name}
         [:p
          [:button.button.is-info.is-fullwidth
           {:on-click #(enact-project proj)}
           name]])])))

(defn- handle-specific-dream [choice]
  (let [[options text-fn post-text-fn effect] @dream
        blurb (text-fn)]
    [:div.box>div.content
     [:h3 "A dreamer visits you..."]
     [:p blurb]
     (when (and (nil? @choice)
                (seq options))
       [:p [:strong "How do you counsel?"]]
       (for [option options]
         ^{:key option}
         [:p
          [:button.button.is-fullwidth.is-primary.is-light
           {:on-click #(reset! choice option)}
           (text/normalize-name option)]]))
     (when (or (some? @choice) (nil? (seq options)))
       [:<>
        (when-let [post-blurb (post-text-fn @choice)]
          [:p post-blurb])
        [:p
         [:button.button.is-fullwidth.is-primary
          {:on-click #(do (reset! herd (effect @choice))
                          (reset! monthstep :upkeep))}
          "The dreamer returns to their rest..."]]])]))

(defn- handle-dream []
  (when (nil? @dream)
    (when (zero? (rand-int 3))
      (when-let [dream* (dream/pick-dream @herd)]
        (reset! dream (dream/marshal-dream @herd dream*)))))
  (if (some? @dream)
    [handle-specific-dream (r/atom nil)]
    (do
      (reset! monthstep :upkeep)
      [:p "Loading..."])))

(defn- handle-upkeep []
  (swap! herd core/apply-herd-upkeep)
  (if (core/has-lost? @herd)
    [:div.box>div.content
     [:h3 "Game over!"]
     [:p meta-text/gameover-text]
     [:button.button.is-primary.is-fullwidth
      {:on-click #(delete-game @game)}
      "Try again!"]]
    (let [herd* @herd
          announcements
          (filter
           some?
           [(when-let [contact (and (core/new-contact? herd*)
                                    (core/get-next-contact herd*))]
              (swap! herd update :contacts conj contact)
              [(str "A new People has made contact!")
               (contact-text/contact->blurb contact)])
            (when (core/should-add-syndicate? herd*)
              (let [votes (core/tally-votes (:individuals herd*))
                    candidates (core/rank-candidates votes)]
                (when-let [candidate (core/select-candidate (:syndicates herd*) candidates)]
                  (swap! herd update :syndicates conj candidate)
                  ["A new syndicate organizes!"
                   (meta-text/announce-syndicate candidate)])))])]
      (when (empty? announcements)
        (reset! monthstep :leave))
      [:div.box>div.content
       [:h3 "End of the month"]
       (for [i (range (count announcements))
             :let [[title announcement] (nth announcements i)]]
         ^{:key i}
         [:<>
          [:h5 title]
          [:p announcement]])
       [:button.button.is-primary.is-fullwidth
        {:on-click #(reset! monthstep :leave)}
        "Proceed to leaving behind resources"]]
      )))

(defn- prompt-leave-behind [resource amount value]
  [:div.field
   [:label.label (text/normalize-name resource)]
   [:div.control
    [prompt-int value amount]]])

(defn- render-leave-heind [herd* stores]
  (let [carry-limit (core/carry-limit herd*)
        carrying (into {} (for [[resource _ value] stores]
                            [resource (int @value)]))
        disabled? (< carry-limit (reduce + 0 (vals carrying)))]
    [:div.box>div.content
     [:h3 "Leave things behind?"]
     [:p (str "Can carry up to " carry-limit " goods.")]
     (when (core/must-leave-some? herd*)
       (let [total (reduce + 0 (map second stores))
             too-many (- total carry-limit)]
         [:p [:em (str "Carrying " total ". " too-many " too many!")]]))
     (doall
      (for [[resource amount value] stores]
        ^{:key resource}
        [prompt-leave-behind resource amount value]))
     [:button.button.is-info.is-fullwidth
      {:on-click #(do (reset! herd herd*)
                      (swap! herd core/keep-and-leave-behind carrying)
                      (reset! monthstep :next))
       :disabled disabled?}
      "Carry this!"]]))

(defn- handle-leave-behind []
  (let [leftovers (get-in @herd [:stores :food] 0)
        herd* (assoc-in @herd [:stores :food] 0)
        location (core/current-location herd*)
        location
        (if (core/local-infra? herd* :granary)
          (update-in location [:stores :food] + leftovers)
          location)
        herd* (core/assoc-location herd* location)
        stores (->> (seq (:stores herd*))
                    (filter (comp pos-int? second))
                    (map #(conj % (r/atom (second %)))))]
    [render-leave-heind herd* stores]))

(defn- handle-next-location []
  [:div.box>div.content
   [:h3 "Choose your next location"]
   (let [location (core/current-location @herd)
         steppe? (= :steppe (:terrain location))
         stage (second (:path @herd))]
     (doall
      (for [i (range (count stage))
            :let [location (get-in @herd [:path 1 i])]]
        ^{:key i}
        [:p
         [:button.button.is-info.is-fullwidth
          {:on-click #(do (swap! herd core/next-location i)
                          (when-not steppe?
                            (swap! herd core/end-month))
                          (reset! monthstep :event))}
          (:name location)]])))])

(defn- playing []
  (case @monthstep
    :event    [handle-event]
    :projects [handle-projects]
    :dream    [handle-dream]
    :upkeep   [handle-upkeep]
    :leave    [handle-leave-behind]
    :next     [handle-next-location]))

(defn- app []
  [:section.section
   [navbar]
   [:hr]
   [:div.block
    (case @state
      :loading    [loading]
      :new-game   [init-new-game]
      :list-games [list-games]
      :credits    [credits]
      :playing    [:div.columns
                   [:div.column.is-half-desktop
                    [print-herd]]
                   [:div.column.is-half-desktop
                    (case @gamestate
                      :intro [intro]
                      :projects [projects]
                      :individuals [individuals]
                      :path [path]
                      :playing [playing])]])]])

(rd/render [app] (js/document.getElementById "app"))
