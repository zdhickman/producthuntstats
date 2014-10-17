(ns producthuntstats.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic base-url "http://www.producthunt.com/?page=")
(def last-page 164) ; actually last page + 1

(defn gen-page [number]
  (str base-url number))

(defn all-pages []
  (map gen-page (range last-page)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def fields [:votes :title :description :comments])

(defn zipmap-fields [l]
  (zipmap fields l))

(defn page-products [page]
  (map zipmap-fields
    (partition 4 
      (map html/text 
        (html/select page 
                      #{[:a.post-url.title] ; title
                        [:span.post-tagline.description] ; description
                        [:span.vote-count] ; votes
                        [:p.comment-count] ; comments
                        ;[:span.user-name] ; TODO: founder boolean
                        })))))

(defn fetch-products []
  (map page-products (map fetch-url (all-pages))))

(defn -main
  "Scrape Product Hunt archive data"
  [& args]
  (println "Hello, World!"))
