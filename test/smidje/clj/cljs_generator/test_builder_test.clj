(ns smidje.clj.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.clj.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]))

(background (clojure.test/report anything) => {})

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
    (eval (generate-single-assert im/simple-failed-checker-function-assertion)) => false))

(facts
  "generate-single-assert with not arrow"
  (fact
    "successful assertion with constant succeeds"
    (eval (generate-single-assert im/simple-addition-successful-not-assertion)) => true)
  (fact
    "failed assertion with constant fails"
    (eval (generate-single-assert im/simple-addition-failed-not-assertion)) => false))

(fact "simple throws form correct"
      (generate-expected-exception im/expected-exception-assertion)
      => '(clojure.test/is (thrown? Exception (smidje.clj.intermediate-maps/foo nil))))

(tabular
 (fact "arrows generate correct equality checks"
       (do-arrow ?arrow) => ?expected)
 ?arrow     ?expected
 '=>        '=
 '=not=>    'not=
 '=bogus=>  (throws Exception))