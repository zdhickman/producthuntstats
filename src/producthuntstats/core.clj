(ns producthuntstats.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(def ^:dynamic base-url "http://www.producthunt.com/?page=")
(def last-page 164) ; actually last page + 
(def num-fields 5)

(defn gen-page [number]
  (str base-url number))

(defn all-pages []
  (map gen-page (range last-page)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def fields [:votes :title :maker :description :comments])

(defn zipmap-fields [l]
  (zipmap fields l))

(defn add-missing-makers [l]
  (if (empty? l) l
    (if (number? (read-string (nth l 3)))
      (cons (nth l 0)
        (cons (nth l 1)
          (cons false
            (cons (nth l 2)
              (cons (nth l 3)
                (add-missing-makers (rest (rest (rest (rest l))))))))))
      (concat (take 5 l) (add-missing-makers (drop 5 l))))))

(defn maybe-replace-maker-span [item]
  (if (= item "\n          The maker of this product joined the comments\n        ")
    true
    item))

(defn smart-partition [numba l]
  (partition numba (map maybe-replace-maker-span (add-missing-makers l))))

(defn page-products [page]
  (map zipmap-fields
    (smart-partition num-fields
      (map html/text 
        (html/select page 
                      #{[:a.post-url.title] ; title
                        [:span.post-tagline.description] ; description
                        [:span.vote-count] ; votes
                        [:p.comment-count] ; comments
                        [:span.user-name] ; maker boolean
                        })))))

(defn fetch-products []
  (map page-products (map fetch-url (all-pages))))

(defn -main
  "Scrape Product Hunt archive data"
  [& args]
  (println "Hello, World!"))
