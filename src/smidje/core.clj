(ns smidje.core
  (:require [smidje.cljs-generator.test-builder :as cljs-builder]
            [smidje.parser.parser :as parser]))

(defmacro fact [& args]
  (-> (parser/parse-fact &form)
      cljs-builder/generate-tests))

(defmacro tabular [& _]
  (parser/tabular* &form))

(comment
  (macroexpand
    '(fact "what a fact"
           (+ 1 1) => 2
           (+ 2 2) =not=> 3
           "hi" => truthy
           true => TRUTHY
           false => FALSEY
           (/ 2 0) => (throws ArithmeticException)
           (/ 4 0) => (throws ArithmeticException "Divide by zero")))

  (macroexpand
   '(tabular "test name"
             (fact "fact name"
                   (+ ?a ?b) => ?c)
             ?a ?b ?c
             1  2  3
             3  4  7))
  )
