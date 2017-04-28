(ns smidje.clj.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.clj.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]
            [smidje.core :as c]))

(defmulti new-report (fn [& args] :default))
(defmethod new-report :default [& args] {})

(with-redefs [clojure.test/report new-report]               ;required to suppress clojure.test output from methods under test

  (facts
    "generate-single-assert with normal arrow"
    (fact
      "successful assertion with constant succeeds"
      (eval (generate-single-assert im/simple-successful-addition-assertion)) => true)
    (fact
      "failed assertion with constant fails"
      (eval (generate-single-assert im/simple-failing-addition-assertion)) => false)
    (fact
      "successful assertion with checker function succeeds"
      (eval (generate-single-assert im/simple-successful-checker-function-assertion)) => true)
    (fact
      "failed assertion with checker function fails"
      (eval (generate-single-assert im/simple-failed-checker-function-assertion)) => false)
    (fact
      "successful assertion with nil value"
      (eval (generate-single-assert
              {:expected-result-form nil
               :expected-result      nil
               :arrow                '=>
               :call-form            nil})) => true)
    (fact
      "failed assertion with nil value"
      (eval (generate-single-assert
              {:expected-result-form nil
               :expected-result      nil
               :arrow                '=>
               :call-form            1})) => false))

  (facts
    "generate-single-assert with not arrow"
    (fact
      "successful assertion with constant succeeds"
      (eval (generate-single-assert im/simple-addition-successful-not-assertion)) => true)
    (fact
      "failed assertion with constant fails"
      (eval (generate-single-assert im/simple-addition-failed-not-assertion)) => false))

  (facts
    "generate-single-assert using anything"
    (fact
      "using anything returns true and calls function"
      (eval (generate-single-assert
              {:expected-result-form 'c/anything
               :expected-result      c/anything
               :arrow                '=>
               :call-form            '(--func--)})) => true
      (provided
        (--func--) => nil :times 1)))

  (fact "simple throws form correct"
        (generate-expected-exception im/expected-exception-assertion)
        => '(clojure.test/is (thrown? Exception (smidje.clj.intermediate-maps/foo nil))))

  (tabular
    (fact "arrows generate correct equality checks"
          (do-arrow ?arrow) => ?expected)
    ?arrow ?expected
    '=> '=
    '=not=> 'not=
    '=bogus=> (throws Exception))

  (facts
    "parse-metaconstant-functions"
    (fact
      "no metaconstants returns empty map"
      (let [mock-map {"func1" :mock1
                      "func2" :mock2}]
        (extract-metaconstant-mocks [] mock-map) => {}))

    (fact
      "only metaconstants returned"
      (let [metaconstants {'func1 '--meta--}
            mock-map {"func1" :mock1
                      "func2" :mock2}]
        (extract-metaconstant-mocks metaconstants mock-map) => {"func1" :mock1})))

  (defn validate-binding [[actual-symbol binding][expected-symbol _]]
    (= [expected-symbol (name expected-symbol)] [actual-symbol (name actual-symbol)]))

  (defn validate-metaconstant-bindings [metaconstants bindings]
    (let [binding-list (partition 2 bindings)]
      (and (every? true? (map validate-binding binding-list metaconstants))
           (even? (count bindings)))))

  (fact
    "generate-metaconstant-bindings creates valid bindings"
    (let [metaconstants {'binding1 '..symbol..
                         'binding2 '--symbol--}]
      (generate-metaconstant-bindings metaconstants) => (partial validate-metaconstant-bindings metaconstants))))

(tabular
  (fact
    "list-contains returns correct results"
    (list-contains? ?list ?object) => ?result)
  ?list     ?object       ?result
  []        1             falsey
  [1 2]     1             truthy
  [1 2]     5             falsey
  nil       2             falsey)

(fact
  "empty assertions results in print"
  (generate-test {:assertions [] :name ..test-name..}) => `(print "warning:" ..test-name.. "does not have any assertions and will be ignored"))