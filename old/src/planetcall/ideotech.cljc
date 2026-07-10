(ns planetcall.ideotech
  (:require [clj-yaml.core :as yaml]
            [clojure.core :refer [slurp]]
            [clojure.set :refer [intersection]]
            [clojure.spec.alpha :as s]))

(s/def ::name string?)
(s/def ::description string?)
(s/def ::flavor string?)
(s/def ::ideotech-detail
  (s/keys :req-un [::name ::description ::flavor]))

(def shortcode-re #"(\w{1,2})([1234])([abcd]?)")
(s/def ::shortcode #(re-matches shortcode-re %))

(defn unpack-shortcode [shortcode]
  {:pre [(s/valid? ::shortcode shortcode)]}
  (let [[_ ideocode tiercode poscode] (re-matches shortcode-re shortcode)]
    [ideocode (Integer/parseInt tiercode) poscode]))

(s/def ::ideotech-lookup (s/map-of ::shortcode ::ideotech-detail))

(def primary-codes ["s" "e" "c" "i" "m"])
(def synergy-codes ["se" "ec" "ci" "im" "ms"])
(def tiers [1 2 3 4])
(def poscodes ["a" "b" "c" "d"])

(def -all-shortcodes
  (flatten [(for [ideocode primary-codes]
              (for [tier tiers]
                (for [pos (if (= tier 1) [nil] (take tier poscodes))]
                  (str ideocode tier pos))))
            (for [ideocode synergy-codes]
              (for [tier (rest tiers)]
                (for [pos (if (= tier 2) [nil] (take (dec tier) poscodes))]
                  (str ideocode tier pos))))]))

(def code->ideology
  {"s"  :science
   "se" :science-ecology
   "e"  :ecology
   "ec" :ecology-contact
   "c"  :contact
   "ci" :contact-industry
   "i"  :industry
   "im" :industry-military
   "m"  :military
   "ms" :military-science})

(def code->opposites
  {"s" ["c" "i"]
   "e" ["i" "m"]
   "c" ["m" "s"]
   "i" ["s" "e"]
   "m" ["e" "c"]})

(def code->pos
  {"" 0
   "a" 0
   "b" 1
   "c" 2
   "d" 3})

(def -raw-details (yaml/parse-string (slurp "doc/planetcall/ideotech-details.yml")))

(defn from-shortcode [shortcode]
  {:pre [(s/valid? ::shortcode shortcode)]
   :post [#(s/valid? (s/nilable ::ideotech-detail) %)]}
  (when-let [[_ ideocode tiercode poscode] (re-matches shortcode-re shortcode)]
    (let [ideology (get code->ideology ideocode)
          raw-tier (Integer/parseInt tiercode)
          pos (get code->pos poscode)
          synergy? (= 2 (count ideocode))
          first? (= raw-tier (count ideocode))
          tier (if (and (not first?) synergy?)
                 (- raw-tier 2)
                 (dec raw-tier))]
      (-> -raw-details ideology (nth tier) (nth pos)))))

(def ideotech->details
  (reduce (fn [details shortcode]
            (assoc details shortcode (from-shortcode shortcode)))
          {}
          -all-shortcodes))

(s/def ::ideotech (-> ideotech->details keys set))
(s/def ::ideotechs (s/coll-of ::ideotech :kind set?))

(def ideologies #{:science :ecology :contact :industry :military})
(s/def ::ideology ideologies)

(defn has-researched-tier
  [researched ideocode tier]
  {:pre [(s/valid? ::ideotechs researched)]}
  (if (= 2 (count ideocode))
    (every? #(has-researched-tier researched (str %) (dec tier)) ideocode)
    (let [candidates
          (if (= 1 tier)
            #{(str ideocode tier)}
            (reduce
             (fn [all poscode] (conj all (str ideocode tier poscode)))
             #{}
             (take tier poscodes)))]
      (some some? (intersection researched candidates)))))

(defn has-forbidden
  [researched ideocode tier]
  {:pre [(s/valid? ::ideotechs researched)]}
  (if (= 2 (count ideocode))
    (some #(has-forbidden researched (str %) tier) ideocode)
    (let [opposites (code->opposites ideocode)]
      (some
       some?
       (for [tier* (range (- 5 tier) 5)]
         (some #(has-researched-tier researched % tier*) opposites))))))

(defn may-research? [researched shortcode]
  (let [[ideocode tier _] (unpack-shortcode shortcode)
        prevtier (dec tier)]
    (and
     ;; must not have already researched it
     (not (researched shortcode))
     ;; must have researched previous tier, if one exists
     (if (< 0 prevtier)
       (has-researched-tier researched ideocode prevtier)
       true)
     ;; must not have forbidden with opposing ideotech
     (not (has-forbidden researched ideocode tier)))))

(defn can-research*
  [researched]
  {:pre [(s/valid? ::ideotechs researched)]
   :post [#(s/valid? ::ideotechs %)]}
  (set (filter
        (partial may-research? researched)
        (keys ideotech->details))))

;; checking requires a scan of all ideotech. let's save ourselves the trouble
(def can-research (memoize can-research*))

(defn count-conflicts* [researched1 researched2]
  (+ (count (filter
             (partial may-research? researched1)
             researched2))
     (count (filter
             (partial may-research? researched2)
             researched1))))

(def count-conflicts (memoize count-conflicts*))
