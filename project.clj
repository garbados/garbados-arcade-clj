(defproject garbados-arcade-clj "0.1.0-SNAPSHOT"
  :description "An arcade of games by garbados."
  :url "http://github.com/garbados/garbados-arcade-clj"
  :license {:name "CC BY-NC-SA 4.0"
            :url "https://creativecommons.org/licenses/by-nc-sa/4.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [clj-commons/clj-yaml "1.0.26"]]
  :repl-options {:init-ns arcade.core}
  :plugins [[lein-cloverage "1.2.2"]]
  :profiles
  {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}
   :cljs
   {:source-paths ["src" "test"]
    :dependencies [[thheller/shadow-cljs "2.26.4"]
                   [reagent "1.2.0"]]}})
