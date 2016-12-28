(ns smidje.cljs.macro-test
  (:require-macros [cljs.test :refer [deftest is]]
                   [smidje.core :refer [fact tabular]]))

(enable-console-print!)

(defn bar []
  1)

(defn thing [var]
  1)

(defn foo []
  (+ (bar) (thing 1)))

(fact "multi-provided"
      (foo) => 2
      (provided
        (bar) => 0
        (thing 1) => 2))

(fact "name"
  (+ 1 1) => 2
  (+ 1 3) =not=> 2
  (+ 1 3) =not> 2)

(tabular "tabularname"
         (fact "factname"
               (+ ?a ?b) => ?c)
         ?a ?b ?c
         1  2  3
         3  4  7)

(fact "expects exception"
      (throw (js/Error. "oh no!")) => (throws js/Error))
