(ns smidje.intermediate-maps)

(def test-metadata
  {:smidje/namespace 'smidje.core-test
   :smidje/file ""})

(def simple-addition-assertion
  {:expected-result-form `'2
   :expected-result 2
   :arrow '=>
   :call-form '(+ 1 1)})

(def simple-addtion-not-assertion
  {:expected-result-form `'3
   :expected-result 3
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

(defn expect-match-map [name & assertions]
  (merge
   test-metadata
   {:tests [{:name name
             :assertions assertions}]}))

(def single-expect-match-map (expect-match-map "addition is simple"
                                               simple-addition-assertion))

(def multiple-expect-match-map (expect-match-map "more addition testing"
                                                 simple-addition-assertion
                                                 ternary-addition-assertion))

(def single-expect-unequal-map (expect-match-map "addition is well defined"
                                                 simple-addtion-not-assertion))

(def single-provided-expect-match-map (expect-match-map "universal question"
                                                        provided-assertion))
