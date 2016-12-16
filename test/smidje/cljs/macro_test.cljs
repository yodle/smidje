(ns smidje.cljs.macro-test
    (:require [cljs.test :refer [deftest is]])
    (:require-macros [smidje.parser :refer [fact tabular]]
                     [smidje.cljs-generator.test-builder :refer [testmacro]]))

;(testmacro {:tests [{:name "mytest", :assertions [{:function-under-test (+ 1 1), :expected-result 2}]}]})

(fact "name"
  (+ 1 1) => 2
  (+ 1 3) =not=> 2
  (+ 1 3) =not> 2)

(println (macroexpand-1 '(tabular "tabularname"
                                  (fact "factname"
                                        (+ ?a ?b) => ?c)
                                  ?a ?b ?c
                                  1 2 3
                                  3 4 7
                                  9 10 19)))
