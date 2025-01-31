(defproject garbados-arcade-clj "0.1.0-SNAPSHOT"
  :description "An arcade of games by garbados."
  :url "http://github.com/garbados/garbados-arcade-clj"
  :license {:name "CC BY-NC-SA 4.0"
            :url "https://creativecommons.org/licenses/by-nc-sa/4.0/"}
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [clj-commons/clj-yaml "1.0.27"]]
  :repl-options {:init-ns arcade.core}
  :plugins [[lein-cloverage "1.2.4"]]
  :profiles
  {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}
   :cljs
   {:source-paths ["src" "test"]
    :dependencies [[thheller/shadow-cljs "2.26.4"]
                   [reagent "1.2.0"]
                   [metosin/reitit "0.7.0-alpha7"]
                   [metosin/reitit-spec "0.7.0-alpha7"]
                   [metosin/reitit-frontend "0.7.0-alpha7"]]}})
