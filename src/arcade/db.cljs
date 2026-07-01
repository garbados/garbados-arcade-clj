(ns arcade.db
  (:require ["pouchdb" :as pouchdb]
            [clojure.edn :as edn]))

(defn init-db [db-name]
  (new pouchdb db-name))

(defn list-docs [db]
  (.then (.allDocs db)
         (fn [rows]
           (vec (map #(.-id %) (.-rows rows))))))

(defn save-doc [db id value]
  (.catch (.put db (js-obj "_id" id
                           "value" (pr-str value)))
          #(.then (.get db id)
                  (fn [doc]
                    (.put db (-> doc
                                 js->clj
                                 (assoc :value (pr-str value))
                                 clj->js))))))

(defn fetch-doc [db id]
  (.then (.get db id)
         (fn [doc]
           (edn/read-string (.-value doc)))))

(defn delete-doc [db id]
  (.then (.get db id) #(.remove db %)))
