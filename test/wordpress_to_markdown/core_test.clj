(ns wordpress-to-markdown.core-test
  (:require [clojure.test :refer :all]
            [wordpress-to-markdown.core :refer [parse-date]]))

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