(ns dimdark.core
  (:require [clojure.spec.alpha :as s]))

(s/def ::name keyword?)
(s/def ::class keyword?)
(s/def ::level (s/int-in 1 6))
(def attributes #{:prowess :alactrity :vigor :spirit :focus :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute pos-int?))
(s/def ::abilities (s/coll-of keyword?))
(s/def ::creature
  (s/keys :req-un [::name
                   ::level
                   ::class
                   ::attributes
                   ::abilities]))

(def kobold-names #{:drg
                    :grp
                    :knz
                    :muu
                    :yap
                    :yip})

(s/def :kobold/name kobold-names)

(def kobold-classes
  #{:mage
    :druid
    :ranger
    :warrior
    :sneak
    :guardian})

(s/def :kobold/class kobold-classes)

(def kobold-name->class
  {:drg :mage
   :grp :druid
   :knz :ranger
   :muu :warrior
   :yap :sneak
   :yip :guardian})

;; kobold growth apportions 18 growth points per level
(def kobold-class-growth
  {:mage
   {:prowess 3
    :alacrity 2
    :vigor 2
    :spirit 3
    :focus 5
    :luck 1}
   :druid
   {:prowess 1
    :alacrity 2
    :vigor 4
    :spirit 5
    :focus 3
    :luck 3}
   :ranger
   {:prowess 3
    :alacrity 5
    :vigor 2
    :spirit 3
    :focus 2
    :luck 4}
   :warrior
   {:prowess 5
    :alacrity 3
    :vigor 3
    :spirit 2
    :focus 2
    :luck 3}
   :sneak
   {:prowess 2
    :alacrity 4
    :vigor 2
    :spirit 2
    :focus 3
    :luck 5}
   :guardian
   {:prowess 4
    :alacrity 1
    :vigor 5
    :spirit 3
    :focus 2
    :luck 3}})

(s/def :kobold/growth
  (s/and
   (s/map-of ::attribute (s/int-in 1 6))
   (s/or :mage (:mage kobold-class-growth)
         :druid (:druid kobold-class-growth)
         :ranger (:ranger kobold-class-growth)
         :warrior (:warrior kobold-class-growth)
         :sneak (:sneak kobold-class-growth)
         :guardian (:guardian kobold-class-growth))))

(s/def ::kobold
  (s/and
   ::creature
   (s/keys :req-un [:kobold/name
                    :kobold/class
                    :kobold/growt
                    ::equipped])))

(s/def ::kobolds
  (s/map-of :kobold/name ::kobold))

(def base-attributes
  (reduce
   #(assoc %1 %2 2)
   {}
   attributes))

(def kobolds
  (reduce
   (fn [kobolds kobold-name]
     (let [klass (kobold-name kobold-name->class)
           growth (klass kobold-class-growth)]
       (assoc kobolds kobold-name
              {:name kobold-name
               :level 1
               :class klass
               :abilities []
               :growth growth
               :attributes (merge-with + base-attributes growth)})))
   {}
   kobold-names))

