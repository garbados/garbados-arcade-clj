(ns planetcall-next.web.scenes.new-game 
  (:require
   [planetcall-next.rules.scenarios :as scenarios]
   [planetcall-next.web.board :as rex]))

(defn init-board-and-game [scene & {:keys [scenario radius]
                                    :or {scenario :standard
                                         radius 64}}]
  (let [{:keys [board coords]} (rex/gen-board scene radius :scenario scenario)
        game (atom (scenarios/init-game-from-scenario coords :standard))]
    {:coords coords
     :board board
     :game game}))
