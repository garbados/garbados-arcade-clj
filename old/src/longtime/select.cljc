(ns longtime.select 
  (:require [clojure.spec.alpha :as s]
            [longtime.core :as core]))

(s/def ::skills (s/map-of ::core/skill nat-int?))

(s/def ::contacts
  (s/or :one ::core/contact
        :many ::core/contacts))

(s/def ::space
  (s/or :one core/space-infra
        :many ::core/space))

(s/def ::infra
  (s/or :one core/buildings
        :many ::core/infra))

(s/def ::power pos-int?)

(s/def ::stores
  (s/map-of (conj core/resources :nutrition)
            (s/or :n pos-int?
                  :x (s/and number? #(< 0 % 1)))))

(s/def ::filter (s/keys :opt-un [::skills
                                 ::stores
                                 ::core/season
                                 ::core/terrain
                                 ::contacts
                                 ::space
                                 ::infra
                                 ::power]))

;; other filter ideas: contacts, space, buildings

(defn passes-filter?
  [herd {:keys [skills
                stores
                season
                terrain
                contacts
                space
                infra
                power]}]
  (and (if terrain
         (= (:terrain (core/current-location herd)) terrain)
         true)
       (if season
         (= (core/get-season herd) season)
         true)
       (every?
        (fn [[resource required]]
          (if (= :nutrition resource)
            (core/herd-has-nutrition? herd required)
            (>= (get-in herd [:stores resource] 0)
                (if (> 1 required 0)
                  (int (* required (count (:individuals herd))))
                  required))))
        (or stores {}))
       (if (and contacts (s/valid? ::contacts contacts))
         (let [[kind x] (s/conform ::contacts contacts)]
           (case kind
             :one (contains? (:contacts herd) x)
             :many (nil? (seq (reduce disj x (:contacts herd))))))
         true)
       (if (and infra (s/valid? ::infra infra))
         (let [[kind x] (s/conform ::infra infra)
               location (core/current-location herd)]
           (case kind
             :one (contains? (:infra location) x)
             :many (nil? (seq (reduce disj x (:infra location))))))
         true)
       (if (and space (s/valid? ::space space))
         (let [[kind x] (s/conform ::space space)]
           (case kind
             :one (contains? (:space herd) x)
             :many (nil? (seq (reduce disj (set x) (:space herd))))))
         true)
       (if power
         (let [location (core/current-location herd)]
           (>= (:power location) power))
         true)
       (reduce
        (fn [ok? [skill required]]
          (and ok?
               (>= (core/collective-skill herd skill)
                   required)))
        true
        (or skills {}))))

(s/fdef passes-filter?
  :args (s/cat :herd ::core/herd
               :filter ::filter)
  :ret boolean?)

(s/def ::comparator #{:< :<= :> :>=})
(def comparator->fn {:< < :<= <= :> > :>= >=})

(s/def ::fulfillment
  (s/or :n ::core/fulfillment
        :comparator (s/tuple ::comparator ::core/fulfillment)))

(s/def ::passions
  (s/or :one ::core/skill
        :many ::core/uses))
(s/def ::-passions ::passions)

(s/def ::traits
  (s/or :one core/traits
        :many ::core/traits))
(s/def ::-trait ::traits)

(s/def ::age
  (s/tuple ::comparator ::core/age))

(s/def ::select (s/keys :opt-un [::traits
                                 ::-trait
                                 ::core/skills
                                 ::fulfillment
                                 ::passions
                                 ::-passions
                                 ::age]))

(defn individual-traits-ok? [individual p? traits]
  (if (and traits (s/valid? ::traits traits))
    (let [[kind x] (s/conform ::traits traits)
          individual-traits (:traits individual #{})]
      (case kind
        :one (p? (get individual-traits x))
        :many (every? p? (for [trait x]
                             (get individual-traits trait)))))
    true))

(s/fdef individual-traits-ok?
  :args (s/cat :individual ::core/individual
               :spec keyword?
               :p? fn?
               :traits ::traits*)
  :ret boolean?)

(defn individual-passions-ok? [individual p? passions]
  (if (and passions (s/valid? ::passions passions))
    (let [[kind x] (s/conform ::passions passions)
          individual-passions (:passions individual #{})]
      (case kind
        :one (p? individual-passions x)
        :many (every? some? (map individual-passions x))))
    true))

(defn passes-select?
  [herd individual {:keys [traits -traits
                           passions -passions
                           skills fulfillment age]}]
  (and (individual-traits-ok? individual some? traits)
       (individual-traits-ok? individual nil? -traits)
       (individual-passions-ok? individual some? passions)
       (individual-passions-ok? individual nil? -passions)
       (if skills
         (let [individual-skills (:skills individual {})]
           (every? true? (for [[skill value] skills]
                           (-> individual-skills (get skill 0) (>= value)))))
         true)
       (if-let [[comparator n] age]
         ((comparator->fn comparator) (core/get-age herd individual) n)
         true)
       (if (and fulfillment (s/valid? ::fulfillment fulfillment))
         (let [[kind x] (s/conform ::fulfillment fulfillment)
               individual-fulfillment (:fulfillment individual 0)
               [comparator n] (if (= :n kind) [:> x] x)
               f (comparator->fn comparator)]
           (f individual-fulfillment n))
         true)))

(s/fdef passes-select?
  :args (s/cat :herd ::core/herd
               :individual ::core/individual
               :select ::select)
  :ret boolean?)

(defn find-individuals
  [herd select]
  (seq
   (filter
    #(passes-select? herd % select)
    (:individuals herd))))

(s/fdef find-individuals
  :args (s/cat :herd ::core/herd
               :select ::select)
  :ret (s/nilable ::core/individuals))

(defn get-cast [herd selects]
  (seq
   (reduce
    (fn [selected select]
      (if-let [individuals
               (seq
                (filter
                 (complement (partial contains? selected))
                 (find-individuals herd select)))]
        (conj selected (rand-nth individuals))
        (reduced nil)))
    #{}
    selects)))

(s/fdef get-cast
  :args (s/cat :herd ::core/herd
               :selects (s/coll-of ::select))
  :ret (s/nilable ::core/individuals))

(defn contains-individual? [personae individual]
  (some
   #(= individual %)
   (reduce concat [] (vals personae))))

(s/fdef contains-individual?
  :args (s/cat :personae (s/map-of keyword? ::core/individuals)
               :individual ::core/individual)
  :ret boolean?)

(s/def :selects/cast
  (s/map-of keyword? ::select))

(s/def :found/cast
  (s/map-of keyword? ::core/individual))

(defn fetch-cast [herd cast-selects]
  (reduce
   (fn [personae [key select]]
     (when personae
       (when-let [selected
                  (first
                   (shuffle
                    (filter
                     (partial contains-individual? personae)
                     (or (find-individuals herd select) []))))]
         (assoc personae key selected))))
   {}
   (seq cast-selects)))

(s/fdef fetch-cast
  :args (s/cat :herd ::core/herd
               :cast-selects :selects/cast)
  :ret (s/nilable :found/cast)
  :fn (fn [{:keys [args ret]}]
        (if ret
          (= (sort (keys ret))
             (sort (keys (:cast-selects args))))
          true)))
