(ns smidje.cljs.macro-test
  (:require-macros [cljs.test :refer [deftest is]]
                   [smidje.core :refer [fact]]))

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