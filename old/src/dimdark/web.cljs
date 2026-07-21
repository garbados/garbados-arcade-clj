(ns dimdark.web
  (:require [arcade.db :as db]
            [arcade.reagent :refer [prompt-text]]
            [arcade.text :as text :refer-macros [inline-slurp]]
            [dimdark.games :as g]
            [dimdark.web.game :as wg]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]))

(defonce db (db/init-db "dimdark"))
(defonce -games (r/atom []))
(defonce -game (r/atom nil))

(defn refresh-saved-games []
  (.then (db/list-docs db)
         #(reset! -games %)))

(defn load-game [save-name]
  (.then (db/fetch-doc db save-name)
         #(do (reset! -game %)
              (rfe/navigate ::game))))

(defn preload-game []
  (when (= 1 (count @-games))
    (.then (db/fetch-doc db (first @-games))
           #(reset! -game %))))

(defn init-new-game [save-name]
  (let [game (g/init-new-game save-name)]
    (db/save-doc db save-name game)
    (reset! -game game)
    (swap! -games conj save-name)
    (rfe/navigate ::game)))

(defn index-view []
  (or (case (count @-games)
        0 (rfe/navigate ::new-game)
        1 (load-game (first @-games)))
      (rfe/navigate ::list-games))
  [:<>])

(def credits
  (inline-slurp "resources/dimdark/credits.txt"))

(defn credits-view []
  [:div.columns>div.column.is-6.is-offset-3>div.box>div.content
   [:h3 "Credits"]
   (for [p (text/collect-text credits)]
     [:p p])])

(def intro-title
  (inline-slurp "resources/dimdark/intro/title.txt"))

(def intro-body
  (inline-slurp "resources/dimdark/intro/body.txt"))

(defn new-game-view []
  (let [-save-name (r/atom "")]
    [:div.columns>div.column.is-6.is-offset-3>div.box>div.content
     [:h3 intro-title]
     (for [p (text/collect-text intro-body)]
       [:p p])
     [:div.field
      [:label.label "What do the kobolds call their dragon?"]
      [:div.control
       [prompt-text -save-name init-new-game]]]
     [:div.field
      [:div.control
       [:button.button.is-fullwidth.is-primary
        {:on-click #(init-new-game @-save-name)}
        "Proceed into the Dimdark..."]]]]))

(defn list-games-view []
  (when (zero? (count @-games))
    (rfe/navigate ::new-game))
  [:div.columns>div.column.is-6.is-offset-3>div.box>div.content
   [:h3 "Saved Games"]
   (for [save-name @-games]
     ^{:key save-name}
     [:div.level
      [:button.button.is-fullwidth.is-link
       {:on-click #(load-game save-name)}
       save-name]
      [:button.button.is-narrow.is-danger
       {:on-click
        (fn [& _]
          (when (js/confirm "Delete this save?")
            (-> (db/delete-doc db save-name)
                (.then refresh-saved-games)
                (.then #(when (= save-name (:name @-game))
                          (reset! -game nil)
                          (rfe/navigate ::index))))))}
       "Delete!"]])])

(def routes
  [["/"
    {:name ::index
     :view index-view}]
   ["/credits"
    {:name ::credits
     :view credits-view}]
   ["/new-game"
    {:name ::new-game
     :view new-game-view}]
   ["/list-games"
    {:name ::list-games
     :view list-games-view}]
   ["/game"
    {:name ::game
     :view (partial wg/game-view -game)}]])

(def router
  (rf/router routes {:data {:coercion rss/coercion}}))

(defn- navbar []
  [:div.box
   [:div.level
    [:div.level-left
     [:div.level-item
      [:h1.title "The Dimdark"]]
     [:div.level-item
      [:a.button.is-primary
       {:href (rfe/href ::new-game)}
       "New Game"]]
     (when (pos-int? (count @-games))
       [:div.level-item
        [:a.button.is-link
         {:href (rfe/href ::list-games)}
         "List Games"]])
     (when (some? @-game)
       [:div.level-item
        [:a.button.is-info
         {:href (rfe/href ::game)}
         "Play Game"]])]
    [:div.level-right
     [:div.level-item
      [:a.button.is-info.is-light
       {:href (rfe/href ::credits)}
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
         "DFB"]]]]]]])

(defonce current-view (r/atom nil))

(defn- app []
  [:<>
   [navbar]
   [:div.block
    (when @current-view
      (when-let [view (-> @current-view :data :view)]
        [view @current-view]))]])

(defn on-navigate [m]
  (reset! current-view m))

(defn- init-app! []
  (rfe/start! router on-navigate {:use-fragment true})
  (-> (refresh-saved-games)
      (.then preload-game)
      (.then #(rd/render [app] (js/document.getElementById "app")))))

(init-app!)
