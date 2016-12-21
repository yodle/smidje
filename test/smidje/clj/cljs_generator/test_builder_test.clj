(ns smidje.clj.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.clj.parser.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]))

(fact "a single expect match arrow fact"
      (generate-single-assert im/simple-addition-assertion)
      => `(cljs.test/is (cljs.core/= (~'+ 1 1) 2)))

(fact "a single not assertion fact"
      (generate-single-assert im/simple-addtion-not-assertion)
      => `(cljs.test/is (cljs.core/not= (~'+ 1 1) 3)))

(fact "simple throws"
      (generate-expected-exception im/expected-exception-assertion)
      => '(cljs.test/is (thrown? InvalidFooError (foo nil))))

(tabular
 (fact "arrows generate correct equality checks"
       (do-arrow ?arrow) => ?expected)
 ?arrow     ?expected
 '=>        'cljs.core/=
 '=not=>    'cljs.core/not=
 '=not>     'cljs.core/not=
 '=bogus=>  (throws Exception))
