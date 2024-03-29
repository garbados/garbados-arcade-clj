(ns longtime.project-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer [deftest is testing]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.properties :as props]
            [longtime.core :as core]
            [longtime.project :as project]
            [arcade.test-util :refer [spec-test-syms]]))

(deftest validate-projects
  (testing "Projects conform to spec"
    (doseq [p project/projects]
      (is
       (s/valid? ::project/project p)
       (s/explain-str ::project/project p)))))

(deftest spec-tests
  (spec-test-syms
   [`project/distribute-experience
    `project/distribute-fulfillment
    `project/can-enact?]))

(defspec test-enact-project 20
  (props/for-all
   [herd (s/gen ::core/herd)]
   (reduce
    (fn [ok? project]
      (and ok?
           (if (project/can-enact? herd project)
             (and
              (s/valid? ::core/herd (project/enact-project herd project))
              (s/valid? ::core/herd (project/do-project herd project)))
             true)))
    true
    project/projects)))
