(ns smidje.cljs-generator.test-builder-test
  (:require [midje.sweet :refer :all]
            [smidje.intermediate-maps :as im]
            [smidje.cljs-generator.test-builder :refer :all]))

(def single-expect-match-cljs
  `(do
     (cljs.test/deftest ~(symbol "addition is simple")
       (cljs.test/is (cljs.core/= (~'+ 1 1) 2)))))

(fact "a single expect match arrow fact"
      (generate-tests im/single-expect-match-map) => single-expect-match-cljs)

(def multiple-expect-match-cljs
  `(do
     (cljs.test/deftest ~(symbol "more addition testing")
       (cljs.test/is (cljs.core/= (~'+ 1 1) 2))
       (cljs.test/is (cljs.core/= (~'+ 1 1 1) 3)))))

(fact "multiple assertion fact"
      (generate-tests im/multiple-expect-match-map) => multiple-expect-match-cljs)

(def single-expect-unequal-cljs
  `(do
     (cljs.test/deftest ~(symbol "addition is well defined")
       (cljs.test/is (cljs.core/not= (~'+ 1 1) 3)))))

(fact "not assertion fact"
      (generate-tests im/single-expect-unequal-map) => single-expect-unequal-cljs)
