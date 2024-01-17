(ns dimdark.web
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [dimdark.core :as d]
            [dimdark.kobolds :as k]
            [dimdark.skills :as sk]
            [arcade.db :as db]))

(def db (db/init-db "dimdark"))
(def games (r/atom []))
(def game (r/atom nil))

(def routes
  []
  #_[["/"
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
       :view game-view}]
     ["/kobolds"
      {:name ::kobolds
       :view kobolds-view}]
     ["/equipment"
      {:name ::equipment
       :view equipment-view}]])

(defn- navbar []
  [:div.level
   [:div.level-left
    [:div.level-item
     [:h1.title "The Dimdark"]]
    [:div.level-item
     [:a.button.is-primary
      {:href (rfe/href ::new-game)}
      "New Game"]]
    (when (pos-int? (count @games))
      [:div.level-item
       [:button.button.is-link
        {:href (rfe/href ::list-games)}
        "List Games"]])
    (when (some? @game)
      [:div.level-item
       [:a.button.is-info
        {:href (rfe/href ::game)}
        "Play Game"]])
    (when (some? @game)
      (for [state [::game ::kobolds ::equipment ::crafting]]
        ^{:key state}
        [:div.level-item
         [:a.button.is-info
          {}
          (text/normalize-name state)]]))]
   [:div.level-right
    [:div.level-item
     [:a.button.is-info.is-light
      {:href (rfe/)}
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

(defn- app []
  [:section.section
   [navbar]
   [:hr]
   [:div.block]])

(def router
  (rf/router routes {:data {:coercion rss/coercion}}))

(defn on-navigate [& _])

(defn- init-app! []
  (rfe/start!
   router
   on-navigate
   ;; set to false to enable HistoryAPI
   {:use-fragment true})
  (rd/render [app] (js/document.getElementById "app")))

(init-app!)
