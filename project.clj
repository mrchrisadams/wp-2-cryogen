(defproject wordpress-to-markdown "0.1.0-SNAPSHOT"
  :description "A library to take a wordpress xml dump and turn each post to markdown for use in static site generators"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [
    [org.clojure/clojure "1.10.0"]
    [org.clojure/data.zip "1.0.0"]
    [selmer "1.12.23"]]
  :repl-options {:init-ns wordpress-to-markdown.core})
