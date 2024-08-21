(ns longtime.contacts
  (:require [arcade.core :refer-macros [inline-slurp]]))

(def auter-contact (inline-slurp "resources/longtime/contacts/auter.txt"))
(def felidar-contact (inline-slurp "resources/longtime/contacts/felidar.txt"))
(def harp-contact (inline-slurp "resources/longtime/contacts/harp.txt"))
(def er'sol-contact (inline-slurp "resources/longtime/contacts/er_sol.txt"))
(def dod-contact (inline-slurp "resources/longtime/contacts/dod.txt"))
(def saurek-contact (inline-slurp "resources/longtime/contacts/saurek.txt"))
(def haroot-contact "TODO")
(def rak-contact "TODO")
(def dabulan-contact "TODO")

(def contact->blurb
  {:auter auter-contact
   :felidar felidar-contact
   :harp harp-contact
   :er'sol er'sol-contact
   :dod dod-contact
   :saurek saurek-contact
   :haroot haroot-contact
   :rak rak-contact
   :dabulan dabulan-contact})
