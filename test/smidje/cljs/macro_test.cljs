(ns smidje.cljs.macro-test
    (:require [cljs.test :refer [deftest is]])
    (:require-macros [smidje.parser :refer [fact provided]]
                     [smidje.cljs-generator.test-builder :refer [testmacro]]))

(defn bar []
  1)

(defn thing [var]
  1)

(defn foo []
  (+ (bar) (thing 1)))


;(testmacro {:tests [
;                    {:name "providedtest",
;                     :assertions
;                           [{:call-form (foo),
;                             :expected-result 2
;                             :arrow =>
;                             :provided [{:mock-function bar
;                                         :return {nil {:result 2
;                                                       :calls 1
;                                                       :arrow =>}}}]}]}]})

;{:bar {:mock-config {nil {:result 3
;                          :calls 1
;                          :arrow =>}}
;       :function bar}}

;(fact "name"
;  (+ 1 1) => 2
;  (+ 1 3) =not=> 2
;  (+ 1 3) =not> 2)

;(println (macroexpand-1 '(fact "name"
;      (foo) => 2
;      (provided
;        (bar 1) => 2))))
;
;(fact "name"
;      (foo) => 2
;      (provided
;        (bar) => 0))

(fact "multi-provided"
      (foo) => 2
      (provided
        (bar) => 0
        (thing 1) => 2))