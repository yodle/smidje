(ns smidje.clj.intermediate-maps)

(def test-metadata
  {:smidje/namespace 'smidje.core-test
   :smidje/file ""})

(def simple-successful-addition-assertion
  {:expected-result-form `'2
   :expected-result 2
   :arrow '=>
   :call-form '(+ 1 1)})

(def simple-failing-addition-assertion
  {:expected-result-form `'2
   :expected-result 3
   :arrow '=>
   :call-form '(+ 1 1)})

(def simple-successful-checker-function-assertion
  {:expected-result-form 'even?
   :expected-result even?
   :arrow '=>
   :call-form '(+ 1 1)})

(def simple-failed-checker-function-assertion
  {:expected-result-form 'odd?
   :expected-result odd?
   :arrow '=>
   :call-form '(+ 1 1)})

(def simple-addition-successful-not-assertion
  {:expected-result-form `'3
   :expected-result 3
   :arrow '=not=>
   :call-form '(+ 1 1)})

(def simple-addition-failed-not-assertion
  {:expected-result-form `'2
   :expected-result 2
   :arrow '=not=>
   :call-form '(+ 1 1)})

(def ternary-addition-assertion
  {:expected-result-form `'3
   :expected-result 3
   :arrow '=>
   :call-form '(+ 1 1 1)})

(def provided-mock-config
  {[21] {:result nil
         :arrow '=>}})

(def provided-assertion
  {:expected-result-form `':answer
   :expected-result :answer
   :arrow '=>
   :call-form '(test-fn 42)
   :provided {:mock-function 'foo
              :return provided-mock-config}})

(defn foo [& args] (throw (Exception.)))

(def expected-exception-assertion
  {:call-form `(foo nil)
   :arrow '=>
   :throws-exception 'Exception})

(defn expect-match-map [name & assertions]
  (merge
   test-metadata
   {:tests [{:name name
             :assertions assertions}]}))

(def single-expect-match-map (expect-match-map "addition is simple"
                                               simple-successful-addition-assertion))

(def multiple-expect-match-map (expect-match-map "more addition testing"
                                                 simple-successful-addition-assertion
                                                 ternary-addition-assertion))

(def single-expect-unequal-map (expect-match-map "addition is well defined"
                                                 simple-addition-successful-not-assertion))

(def single-provided-expect-match-map (expect-match-map "universal question"
                                                        provided-assertion))
