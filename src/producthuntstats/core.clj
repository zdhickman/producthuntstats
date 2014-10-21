(ns producthuntstats.core
  (:gen-class)
  (:require [net.cgrand.enlive-html :as html]))

(use 'clojure.java.io)

(def ^:dynamic base-url "http://www.producthunt.com/?page=")
(def last-page 164) ; actually last page + 
(def num-fields 6)

(defn gen-page [number]
  (str base-url number))

(defn all-pages []
  (map gen-page (range last-page)))

(defn fetch-url [url]
  (html/html-resource (java.net.URL. url)))

(def fields [:votes :link :title :maker :description :comments])

(defn String->Number [str]
  (try 
    (let [n (read-string str)]
      (if (number? n) n nil))
    (catch Exception e nil)))

(defn add-missing-makers [l]
  (if (empty? l) l
    (if (number? (String->Number (nth l 4)))
      (cons (nth l 0)
        (cons (nth l 1)
          (cons false
            (cons (nth l 2)
              (cons (nth l 3)
                (cons (nth l 4)
                (add-missing-makers (rest (rest (rest (rest (rest l))))))))))))
      (concat (take num-fields l) (add-missing-makers (drop num-fields l))))))

(defn maybe-replace-maker-span [item]
  (if (= item "\n          The maker of this product joined the comments\n        ")
    true
    item))

(defn smart-partition [numba l]
  (partition numba (map maybe-replace-maker-span (add-missing-makers l))))

(defn text* [all-things]
  (if (empty? all-things) all-things
    (let [one-thing (first all-things)
          maybe-href (:href (:attrs one-thing))]
      (if (not (not maybe-href))
        (cons maybe-href 
          (cons (html/text one-thing)
            (text* (rest all-things))))
        (cons (html/text one-thing)
          (text* (rest all-things)))))))


(defn page-products [page]
  (map #(zipmap fields %)
    (smart-partition num-fields
      (text* 
        (html/select page 
                      #{[:a.post-url.title] ; title
                        [:span.post-tagline.description] ; description
                        [:span.vote-count] ; votes
                        [:p.comment-count] ; comments
                        [:span.user-name] ; maker boolean
                        })))))

(defn fetch-products []
  (map page-products (map fetch-url (all-pages))))

(defn fetch-n-write []
  (with-open [wrtr (writer (str (System/getProperty "user.dir")
                              "/data/scraped.txt"))]
    (.write wrtr (with-out-str (pr (fetch-products)))))
  (println "Wrote scraped data to file."))

(defn product-urls [products]
  (map #(get % :link) products))

(defn -main
  "Scrape Product Hunt archive data"
  [& args]
  (println "Hello, World!"))
