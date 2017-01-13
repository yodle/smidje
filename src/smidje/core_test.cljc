(ns smidje.core-test
  (:require
    #?(:cljs [smidje.cljs-generator.test-builder])
    #?(:cljs [cljs.test :refer [inc-report-counter! get-current-env]])
    #?(:clj [smidje.parser.parser :as parser])))

(defmulti reporter (fn [& args] :default) :default {})
(defmethod reporter :default [report]
  (println report)
  #?(:cljs (println (get-current-env)))
  #?(:cljs (inc-report-counter! (:type report))))

(defmacro fact [& args]
  (let [test-configuration# (parser/parse-fact &form)
        test-name           (gensym)]
  `(cljs.core/let [test-configuration# ~test-configuration#]
      (cljs.test/deftest ~test-name
        (binding [cljs.test/report reporter]
          (smidje.cljs-generator.test-builder/generate-tests test-configuration#))))))

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
