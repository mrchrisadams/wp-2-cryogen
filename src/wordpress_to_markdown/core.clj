(ns wordpress-to-markdown.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]))

(def blog-file "./wp-export.xml")

(io/resource blog-file)

(defn load-xml-export
  "Load the xml file and return an map we can query using
  clojure.data.zip.xml's xml query tool."
  [file-path]
  (-> file-path
      io/resource
      io/file
      xml/parse
      zip/xml-zip))

; I want a vector of maps, where each map has all the attributes of the items.
(defn posts
  [parsed-xml]
  (zip-xml/xml-> parsed-xml
    :channel
    :item [(keyword "wp:post_type") "post"]))

(defn post->map
  "Return a map for the corresponding post we pass in."
  [post]
  {:title (zip-xml/xml1-> post :title zip-xml/text)
   :content (zip-xml/xml1-> post (keyword "content:encoded") zip-xml/text)})