(ns producthuntstats.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic base-url "http://www.producthunt.com/?page=")
(def last-page 165) ; actually last page + 1

(defn gen-page [number]
  (str base-url number))

(defn all-pages []
  (map gen-page (range last-page)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
