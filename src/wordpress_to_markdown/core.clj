(ns wordpress-to-markdown.core
  (:require [clojure.java.io :as io]
            [clojure.xml :as xml]
            [clojure.zip :as zip]
            [clojure.data.zip.xml :as zip-xml]
            [selmer.parser :as sp]))

(def blog-file "./wp-export.xml")

(defn load-xml-export
  "Load the xml file and return an map we can query using
  clojure.data.zip.xml's xml query tool."
  [file-path]
  (-> file-path
      io/resource
      io/file
      xml/parse
      zip/xml-zip))

(defn posts-from-xml
  "Returns a lazy seq of maps that correspond to each post item in the xml, that is published"
  [parsed-xml]
  (zip-xml/xml-> parsed-xml
                 :channel
                 :item [(keyword "wp:post_type") "post"]
                 :item [(keyword "wp:status") "publish"]
                 clojure.zip/node))

(defn parse-date
  [date-string]
  (first (clojure.string/split date-string #" ")))

(defn parse-path
  "Return the desired filename for the post, based on the name and slug"
  [post-map]
  (str (parse-date (post-map :date)) "-" (post-map :post-slug) ".md"))

(defn render-into-md-template
  [post-map]
  (let [ctx (dissoc post-map :content :status :post-slug :is-markdown)]
    (sp/render-file "./markdown_template.md" {:content (post-map :content) :ctx ctx})))


(defn file-path-for-post
  [post-map]
  (let [tmpl-string (render-into-md-template post-map)
        post-path (parse-path post-map)]
    post-path))


(defn fetch-first-post
  "Return the first post from the xml export."
  [posts]
  (first posts))

(defn posts
  "Accept a file name, and return a sequence of maps representing
  the parsed blog posts"
  [file-path]
  (-> file-path
    load-xml-export
    posts-from-xml))

(defn tag-is [name]
  #(= (:tag %) name))

(defn content-for-tag [name post]
    "Return the value if we end up with a single node to pull
    content from, otherwise return the collection."
    (let [content
      (filter (tag-is name) (:content post))]
    (if (= (count content) 1)
      (first (:content (first content)))
      content)))


(defn tag-to-map
  [tag-vec]
  {
    (first (:content (first tag-vec)))
    (first (:content (second tag-vec)))})

(defn postmeta-tags
  "return meta tags for a given post"
  [post]
  (let [tags (filter #(= (:tag %) :wp:postmeta ) (:content post))]
    (map
      tag-to-map
      (map :content tags))))

(defn post-is-markdown?
  "Return boolean for whether post is written as markdown or not"
  [post]
  (let [postmeta (postmeta-tags post)
        k "_wpcom_is_markdown"]
      postmeta
      (boolean (get (first postmeta) k))
    ))

(defn post-not-ready?
  "Return a boolean, if a post is likely to fail the compilation
  step with cryogen. Crygoen will not import a post if it has no title."
  [post]
  (or
  ; (post-has-empty-title post)
  )
  )

(defn post->map
  "Return a simplified map for the corresponding post we pass in."
  [post]

  {:author        (content-for-tag :dc:creator post)
   :title         (content-for-tag :title post)
   :content       (content-for-tag :content:encoded post)
   :status        (content-for-tag :wp:status post)
   :date          (content-for-tag :wp:post_date post)
   :publish-date  (content-for-tag :pubDate post)
   :post-slug     (content-for-tag :wp:post_name post)
   :is-markdown   (post-is-markdown? post)
   :draft?        (post-not-ready? post)
   })


(defn write-post-to-file
  "Writes the provided post to the given output directory"
  [post-map output-directory]
  (spit
    (str output-directory (file-path-for-post post-map))
    (render-into-md-template post-map)))

(defn write-posts-to-markdown-files
  "Accepts a path to an wordpress xml file, output directory,
  and writes the published posts to markdown files in the given
  target directory"
  [posts output-directory]
  (doseq [post posts]

    (write-post-to-file (post->map post) output-directory)
    ))