(ns smidje.cljs.macro-test
  (:require-macros [cljs.test :refer [deftest is]]
                   [smidje.parser :refer [fact]]
                   [smidje.cljs-generator.test-builder :refer [testmacro]]))

;(testmacro {:tests [{:name "mytest", :assertions [{:function-under-test (+ 1 1), :expected-result 2}]}]})

(fact "name"
  (+ 1 1) => 2
  (+ 1 3) =not=> 2
  (+ 1 3) =not> 2)

(fact "expects exception"
  (throw (js/Error. "oh no!")) => (throws js/Error))
