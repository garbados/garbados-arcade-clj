(ns arcade.test-utils
  (:require [clojure.spec.alpha :as spec]
            [clojure.spec.test.alpha :as spec-test]
            [clojure.string :as string]
            [clojure.test :refer [is testing]]))

(def default-test-opts {:num-tests 100})

(defn failed-checks [sym opts]
  (->> (spec-test/check sym {:clojure.spec.test.check/opts opts})
       (map spec-test/abbrev-result)
       (filter :failure)))

(defn stest-symbol!
  [sym opts]
  (testing (str sym)
    (let [{:keys [failure]} (first (failed-checks sym opts))]
      (is (nil? failure)
          (ex-message failure)))))

(defn stest-ns!
  ([ns-name]
   (stest-ns! ns-name default-test-opts))
  ([ns-name opts]
   (doseq [[sym var-ref] (ns-publics ns-name)
           :let [spec (spec/get-spec var-ref)
                 {no-stest? :no-stest} (meta var-ref)]
           :when (and (fn? @var-ref)
                      (not no-stest?))]
     (testing (str ns-name "/" sym)
       (if spec
         (stest-symbol! (symbol var-ref) opts)
         (is (some? spec) (str sym " has no spec!")))))))

(defn test-game!
  ([game-name]
   (test-game! game-name #{}))
  ([game-name except-ns]
   (doall
    (->> (all-ns)
         (map ns-name)
         (map name)
         (filter #(string/starts-with? % game-name))
         (filter #(not (string/ends-with? % "-test")))
         (map symbol)
         (filter #(not (contains? except-ns %)))
         (map stest-ns!)))))
