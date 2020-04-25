(ns wordpress-to-markdown.core-test
  (:require [clojure.test :refer :all]
            [wordpress-to-markdown.core :refer :all]))

(deftest a-test
  (testing "FIXME, I fail."
    (is (= 0 0))

))
(deftest test-date-parse
  (testing "Parse date"
  (is (=
    "2017-04-30"
    (parse-date "2017-04-30 16:22:23")
))))

(deftest test-hashmaps-from-xml
  (testing "Loads xml to provide sequence of maps"
    (is (= (seq? (posts blog-file)) true))
    (is (= (map? (first (posts blog-file))) true))))