(ns longtime.scene 
  (:require [clojure.edn :as edn]
            [clojure.set :refer [difference]]
            [clojure.spec.alpha :as s]
            [longtime.core :as core]
            [longtime.select :as select]))

(s/def ::traits*
  (s/or :one ::core/trait
        :many ::core/traits))
(s/def ::+trait ::traits*)
(s/def ::-trait ::traits*)
(s/def ::+fulfillment int?)
(s/def ::+skills (s/map-of ::core/skill (s/int-in (- core/max-skill) (inc core/max-skill))))
(s/def ::passions*
  (s/or :one ::core/skill
        :many ::core/skills))
(s/def ::+passion ::passions*)
(s/def ::-passion ::passions*)
(s/def :effects/cast
  (s/map-of keyword?
            (s/keys :opt-un [::+trait
                             ::-trait
                             ::+fulfillment
                             ::+skills
                             ::+passion
                             ::-passion])))

(s/def ::+resources (s/map-of core/resources int?))
(s/def ::-infra
  (s/or :random true?
        :one ::core/infra
        :many (s/coll-of ::core/infra :kind set?)))

(s/def :effects/herd
  (s/keys :opt-un [::+resources
                   ::+fulfillment
                   ::-infra]))

(s/def :found/herd ::filter)
(s/def ::match
  (s/keys :req-un [:found/cast
                   :found/herd]))
(s/def ::effect
  (s/keys :req-un [:effects/cast
                   :effects/herd]))
(s/def ::scene-config
  (s/keys :req-un [::match
                   ::effect]))

(defn scene-valid? [herd scene-config]
  (and
   (some? (select/fetch-cast herd (get-in scene-config [:match :cast] {})))
   (select/passes-filter? herd (get-in scene-config [:match :herd] {}))))

(s/fdef scene-valid?
  :args (s/cat :herd ::core/herd
               :scene ::scene-config)
  :ret boolean?)

(defn apply-cast-effect [herd scene-config]
  (let [{{cast-select :cast} :match
         {cast-effect :cast} :effect} scene-config
        found-cast (select/fetch-cast herd cast-select)]
    (reduce
     (fn [herd [key {:keys [+trait -trait +fulfillment +skills +passion -passion]}]]
       (core/update-individual
        herd
        (get found-cast key)
        #(cond-> %
           (keyword? +trait)
           (update :traits conj +trait)
           (seq? +trait)
           (update :traits into (set +trait))
           (keyword? -trait)
           (update :traits disj -trait)
           (seq? -trait)
           (update :traits difference (set -trait))
           +fulfillment
           (update :fulfillment + +fulfillment)
           +skills
           (update :skills (partial merge-with +) +skills)
           (keyword? +passion)
           (update :passions conj +passion)
           (seq? +passion)
           (update :passions into (set +passion))
           (keyword? -passion)
           (update :passions disj -passion)
           (seq? -passion)
           (update :passions difference (set -passion)))))
     herd
     cast-effect)))

(defn apply-herd-effect [herd scene-config]
  (let [{{herd-effect :herd} :effect} scene-config
        {:keys [+resources +fulfillment -infra]} herd-effect]
    (cond-> herd
      +resources
      (update :stores (partial merge-with +) +resources)
      +fulfillment
      (core/update-individuals #(core/inc-fulfillment % 5))
      (true? -infra)
      (core/update-current-location :infra (comp set rest shuffle vec))
      (keyword? -infra)
      (core/update-current-location :infra disj -infra)
      (seq? -infra)
      (core/update-current-location :infra difference -infra))))

(defn apply-scene-effect [herd scene-config]
  (let [{{cast-effect :cast herd-effect :herd} :effect} scene-config]
    (cond-> herd
      cast-effect (apply-cast-effect scene-config)
      herd-effect (apply-herd-effect scene-config))))

(s/fdef apply-scene-effect
  :args (s/cat :herd ::core/herd
               :scene ::scene-config)
  :ret ::core/herd)

(defmacro slurp-scene [path]
  {:config
   (edn/read-string
    (clojure.core/slurp `~(str "resources/" path "/config.edn")))
   :template
   (clojure.core/slurp `~(str "resources/" path "/text.mustache"))})
