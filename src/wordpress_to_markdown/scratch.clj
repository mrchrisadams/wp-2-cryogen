; add a basic namespace, I guess
(ns wordpress-to-markdown.core)

(require '[clojure.java.io :as io])
(require '[clojure.xml :as xml])
(require '[clojure.zip :as zip])
(require '[clojure.data.zip.xml :as zip-xml])
(require '[clojure.pprint :as pp])

(def blog-file "./wp-export.xml")

(def pp clojure.pprint/pprint)

(def root (-> blog-file io/resource io/file xml/parse zip/xml-zip))

; referring to an examples from the source for clojure.data.zip.xml
; https://github.com/clojure/data.zip/blob/master/src/test/clojure/clojure/data/zip/xml_test.clj

; (deftest test-xml1->
;   (is (= (xml1-> atom1 :entry :author :name text)
;          "Chouser")))


; I want a vector of maps, where each map has all the attributes of the items.


; gives the textual content for the first of of all the items
(zip-xml/xml1-> root :channel :item (keyword "content:encoded") zip-xml/text)

; gives all the textual content for of all the items
(zip-xml/xml-> root :channel :item (keyword "content:encoded") zip-xml/text)

; gives the first matching item title
(zip-xml/xml1-> root :channel :item :title zip-xml/text)

; gives ALL the titles
(zip-xml/xml-> root :channel :item :title zip-xml/text)

; gives ALL the titles for the posts with the type "post"
(zip-xml/xml-> root :channel
  :item [(keyword "wp:post_type") "post"]
  :title zip-xml/text)

; returns a count of only the titles for the posts with the type "post"
(count
  (zip-xml/xml-> root :channel
    :item [(keyword "wp:post_type") "post"]
    :title zip-xml/text))

; Don't do this. It hoses the memory.
(zip-xml/xml-> root :channel
  :item [(keyword "wp:post_type") "post"])

; This gives me all the text I might want, just mashed together. No good.
(first (zip-xml/xml-> root :channel :item [(keyword "wp:post_type") "post"] zip-xml/text))

; This give me the map I want, but only for the first entry. Asking for more also hoses the memory

(pp (first (first (zip-xml/xml-> root :channel
  :item [(keyword "wp:post_type") "post"]))))

;
(def posts (first (zip-xml/xml-> root :channel
:item [(keyword "wp:post_type") "post"])))

; takes ages, is huge
(pp posts)

; is a vector
(type posts)

; returns a single post
(let [[post & rest] posts]
  post)

; would expect to return the next post, but doesn't :(
(let [[post & rest] posts]
    (first rest))



  ;


(let [loaded-posts (posts (load-xml-export blog-file))
[post & rest] loaded-posts]
(post->map post))

; inspect a single post
; (let [[post & rest] posts]
;   (post->map post))

; iterate through the posts
; (pp (for [post posts]
;   (post->map post)))


; we'd typically render the file here now, as a markdown template.
; because wordpress outputs html, we reuse it

