(ns smidje.intermediate-maps)

(def test-metadata
  {:smidje/namespace 'smidje.core-test
   :smidje/file ""})

(defn simple-addition-assertion [under-test]
  {:expected-result-form '2
   :expected-result 2
   :arrow '=>
   :call-form '(+ 1 1)
   :function-under-test under-test
   :position 1})

(defn ternary-addition-assertion [under-test]
  {:expected-result-form '3
   :expected-result 3
   :arrow '=>
   :call-form '(+ 1 1 1)
   :function-under-test under-test
   :position 2})

(defn single-expect-match-map [simple-addition-fn]
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2)
    :tests [{:name "addition is simple"
             :assertions [(simple-addition-assertion simple-addition-fn)]}]}))

(defn multiple-expect-match-map [simple-addition-fn ternary-addition-fn]
  (merge
   test-metadata
   {:smidje/source '(fact (+ 1 1) => 2 (+ 1 1 1) => 3)
    :tests [{:name "more addition testing"
             :assertions [(simple-addition-assertion simple-addition-fn)
                          (ternary-addition-assertion ternary-addition-fn)]}]}))
