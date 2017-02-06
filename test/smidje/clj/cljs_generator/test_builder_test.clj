(ns smidje.clj.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.clj.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]))

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
    ?arrow ?expected
    '=> '=
    '=not=> 'not=
    '=bogus=> (throws Exception))

  (facts
    "parse-metaconstant-functions"
    (fact
      "no metaconstants returns empty map"
      (let [mock-map {:func1 :mock1
                      :func2 :mock2}]
        (parse-metaconstant-functions [] mock-map) => {}))

    (fact
      "only metaconstants returned"
      (let [metaconstants [:func1]
            mock-map {:func1 :mock1
                      :func2 :mock2}]
        (parse-metaconstant-functions metaconstants mock-map) => {:func1 :mock1}))))