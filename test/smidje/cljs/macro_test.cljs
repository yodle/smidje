(ns smidje.cljs.macro-test
    (:require [cljs.test :refer [deftest is]]
              [cljs.test :refer [deftest is]])
    (:require-macros [smidje.parser :refer [fact]]
                     [smidje.cljs-generator.test-builder :refer [testmacro]]))

(defn bar []
  1)

(defn foo []
  (bar))


(testmacro {:tests [
                    {:name "providedtest",
                     :assertions
                           [{:call-form (foo),
                             :expected-result 2
                             :arrow =>
                             :provided [{:mock-function bar
                                         :return {nil {:result 2
                                                       :calls 2
                                                       :arrow =>}}}]}]}]})

;(testmacro {:tests [
;                    {:name "mytest",
;                     :assertions
;                           [{:call-form (+ 1 1),
;                             :expected-result 2}]}]})

;(fact "name"
;  (+ 1 1) => 2
;  (+ 1 3) =not=> 2
;  (+ 1 3) =not> 2)