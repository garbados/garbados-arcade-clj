(ns dimdark.web
  (:require [arcade.db :as db]
            [arcade.reagent :refer [prompt-text]]
            [clojure.string :as string]
            [dimdark.games :as g]
            [dimdark.text.credits :as credits]
            [dimdark.text.intro :as intro]
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

(defn set-initial-game []
  (or (case (count @-games)
        0 (rfe/navigate ::new-game)
        1 (load-game (first @-games)))
      (rfe/navigate ::list-games)))

(defn init-new-game [save-name]
  (let [game (g/init-new-game save-name)]
    (db/save-doc db save-name game)
    (reset! -game game)
    (swap! -games conj save-name)
    (rfe/navigate ::game)))

(defn credits-view []
  [:div.columns>div.column.is-10.is-offset-1>div.box>div.content
   [:h3 credits/title]
   [:p credits/thanks]
   [:h4 credits/acknowledgements]
   [:p credits/influences]])

(defn index-view []
  (set-initial-game)
  [:<>])

(defn new-game-view []
  (let [-save-name (r/atom "")]
    [:div.columns>div.column.is-10.is-offset-1>div.box>div.content
     [:h3 intro/title]
     [:p intro/section1]
     [:p intro/section2]
     [:p intro/section3]
     [:p intro/section4]
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
  [:div.columns>div.column.is-10.is-offset-1>div.box>div.content
   [:h3 "Saved Games"]
   (for [save-name @-games]
     ^{:key save-name}
     [:div.level
      [:button.button.is-fullwidth.is-link
       {:on-click #(load-game save-name)}
       save-name]
      [:button.button.is-narrow.is-danger
       {:on-click
        #(when (js/confirm "Delete this save?")
           (.then (db/delete-doc db save-name)
                  (fn [& _] (refresh-saved-games))))}
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
   #_["/game"
      {:name ::game
       :view game-view}]])

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
  (rfe/start!
   router
   on-navigate
   ;; set to false to enable HistoryAPI
   {:use-fragment true})
  (.then (refresh-saved-games)
         #(rd/render [app] (js/document.getElementById "app"))))

(init-app!)
