(ns smidje.intermediate-maps)

(def test-metadata
  {:smidje/namespace 'smidje.core-test
   :smidje/file ""})

(def simple-addition-assertion
  {:expected-result-form '2
   :expected-result 2
   :arrow '=>
   :call-form '(+ 1 1)
   :function-under-test (clojure.core/fn [] (+ 1 1))
   :position 1})

(def ternary-addition-assertion
  {:expected-result-form '3
   :expected-result 3
   :arrow '=>
   :call-form '(+ 1 1 1)
   :function-under-test (clojure.core/fn [] (+ 1 1 1))
   :position 2})

(def single-expect-match-map
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2)
    :tests [:description "addition is simple"
             :assertions [simple-addition-assertion]]}))

(def multiple-expect-match-map
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2 (+ 1 1 1) => 3)
    :tests [:description "more addition testing"
             :assertions [simple-addition-assertion
                          ternary-addition-assertion]]}))
