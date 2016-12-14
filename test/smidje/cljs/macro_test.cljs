(ns smidje.cljs.macro-test
    (:require [cljs.test :refer [deftest is]])
    (:require-macros [smidje.parser :refer [fact]]
                     [smidje.cljs-generator.test-builder :refer [testmacro]]))

;(testmacro {:tests [{:name "mytest", :assertions [{:function-under-test (+ 1 1), :expected-result 2}]}]})

(fact "name"
  (+ 1 1) => 2
      (+ 1 2) =not=> 5)