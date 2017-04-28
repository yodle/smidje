(ns smidje.cljs.macro-test
  (:require [smidje.core :refer-macros [fact tabular] :as core]))

(enable-console-print!)

(defn bar []
  1)

(defn thing [var]
  1)

(defn foo []
  (+ (bar) (thing 1)))

(defn anything [a b]
  (- a b))

(defn two-bars []
  (+ (bar) (bar)))

(defn n-bars [times-called]
  (dotimes [n times-called]
    (bar)))

(fact "multi-provided"
      (foo) => 2
      (provided
        (bar) => 0
        (thing 1) => 2))

(fact "arrows"
  (+ 1 1) => 2
  (+ 1 3) =not=> 2)

(tabular "tabularname"
         (fact "factname"
               (+ ?a ?b) => ?c)
         ?a ?b ?c
         1  2  3
         3  4  7)

(fact "truthy and falsey"
  true => truthy
  true => TRUTHY
  false => falsey
  false => FALSEY
  nil => falsey
  1 => truthy
  "text" => truthy)

(fact "expects exception"
  (throw (js/Error. "oh no!")) => (throws js/Error))

(fact "even is even"
      2 => even?
      3 =not=> even?
      (+ 3 2) => #(= 5 %))

(fact
  "provided works with truth checks"
  (bar) => truthy
  (bar) => falsey (provided (bar) => nil))

(fact
  "meta constant"
  (#(identity %) ..test..) => ..test..)

(fact
  "meta constant function"
  (#(--func-- %) ..test..) => 1
  (provided
    (--func-- ..test..) => 1))

(fact
  "metaconstant not equal"
  (thing 1) =not=> ..result..
  (provided
    (thing 1) => ..badresult..))

(fact
  "times checking"
  (two-bars) => 4
  (provided
    (bar) => 2 :times 2
    (foo) => ..whatever.. :times 0
    (thing "foo") => ..no1cur.. :times (range)))

(fact
  "more times checking"
  (n-bars 5) => nil?
  (provided
    (bar) => 2 :times (range 5)))

(fact
  "expects exception when thrown by provided"
  (bar) => (throws js/Error)
  (provided
    (bar) =throws=> (js/Error)))

(fact "expected empty fact warning")

(fact "test-anything provided"
      (anything 1 2) => 2
      (provided
        (anything core/anything 2) => 2))

(fact "test-anything returned"
      (anything 1 2) => core/anything
      (provided
        (anything 1 2) => nil))
