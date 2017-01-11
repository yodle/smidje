(ns smidje.cljs.macro-test
  (:require [smidje.core-test :refer-macros [fact tabular]]
            [cljs.test :refer-macros [deftest testing is]]))

(enable-console-print!)

(defn bar []
  1)

(defn thing [var]
  1)

(defn foo []
  (+ (bar) (thing 1)))

(println (macroexpand-1 '(fact "name"
  (+ 1 1) => 2
  (+ 1 3) =not=> 2)))

(fact "name"
      (+ 1 1) => 2
      (+ 1 3) =not=> 2)

;(defn t [name assert]
;  (testing name
;    (is assert)))
;
;(defn func [name assert]
;  (let [a 1]
;  (deftest smidje-test
;    (t name assert))))

;(fact "multi-provided"
;      (foo) => 2
;      (provided
;        (bar) => 0
;        (thing 1) => 2))
;
;(tabular "tabularname"
;         (fact "factname"
;               (+ ?a ?b) => ?c)
;         ?a ?b ?c
;         1  2  3
;         3  4  7)
;
;(fact "truthy and falsey"
;  true => truthy
;  true => TRUTHY
;  false => falsey
;  false => FALSEY
;  nil => falsey
;  1 => truthy
;  "text" => truthy)
;
;(fact "expects exception"
;  (throw (js/Error. "oh no!")) => (throws js/Error))
;
;(fact "even is even"
;      2 => even?
;      3 =not=> even?
;      (+ 3 2) => #(= 5 %))
