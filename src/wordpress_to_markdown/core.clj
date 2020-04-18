(ns wordpress-to-markdown.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]
            [selmer.parser :as sp]))

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
  "Returns a lazy seq of maps that correspond to each post item in the xml"
  [parsed-xml]
  (zip-xml/xml-> parsed-xml
    :channel
    :item [(keyword "wp:post_type") "post"]))

(defn is-markdown-status
  [post]
  ; TODO
  ; :markdown (zip-xml/xml1-> post (keyword "wp:postmeta") (keyword "wp:meta_key")
  ; <wp:meta_key>_wpcom_is_markdown</wp:meta_key>
  )

(defn post->map
  "Return a map for the corresponding post we pass in."
  [post]

  {
    :author      (zip-xml/xml1-> post (keyword "dc:creator")  zip-xml/text)
    :title       (zip-xml/xml1-> post :title zip-xml/text)
    :content     (zip-xml/xml1-> post (keyword "content:encoded") zip-xml/text)
    :status      (zip-xml/xml1-> post (keyword "wp:status") zip-xml/text)
    :date        (zip-xml/xml1-> post (keyword "wp:post_date") zip-xml/text)
    :post-slug   (zip-xml/xml1-> post (keyword "wp:post_name") zip-xml/text)
    :is-markdown (post-is-markdown? post)
    })

(defn render-into-md-template
  [post-map]
  (let [ctx (dissoc post-map :content)]
  (sp/render-file "./markdown-template.md" {:content (post-map :content) :ctx ctx})))

(defn parse-date
  [date-string]
  (first (clojure.string/split date-string #" ")))

(defn parse-path
  "Return the desired filename for the post, based on the name and slug"
  [post-map]
  (str (parse-date (post-map :date)) "-" (post-map :post-slug) ".md"))

(defn file-path-for-post
  [post-map]
  (let [tmpl-string (render-into-md-template post-map)
        post-path (parse-path post-map)]
        post-path))

(defn write-post-to-file
  [post-map output-directory]
  (spit (str output-directory (file-path-for-post post-map)) (render-into-md-template post-map)))

(defn post-is-markdown?
  "Return boolean for whether post is written as markdown or not"
  [post]
  (boolean (read-string
    (zip-xml/xml1-> post
      (keyword "wp:postmeta")
      ; only return the value that marks if it's markdown
      [(keyword "wp:meta_key") "_wpcom_is_markdown"]
      ; then fetch the value out the xml
      (keyword "wp:meta_value")
      zip-xml/text

    ))))

(defn write-posts-to-markdown-files
  "Accepts a path to an wordpress xml file, output directory,
  and writes the published posts to markdown files in the given
  target directory"
  [wp-export-xml-file output-directory]
  (doseq [post (posts (load-xml-export blog-file))
          post-map (post->map post)]
    (write-post-to-file post output-directory)))

(let [[post & rest] (posts (load-xml-export blog-file))]
  (post-is-markdown? post)
)

(defn fetch-first-post
  "Return the first post from the xml export."
  [post]
  (let [[post & rest] (posts (load-xml-export blog-file))]
    (clojure.zip/node post)))

(clojure.pprint/pprint (:content (fetch-first-post blog-file)))
