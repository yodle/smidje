(ns smidje.clj.cljs-generator.cljs-syntax-converter-test
  (:require [midje.sweet :refer :all]
            [smidje.cljs-generator.cljs-syntax-converter :refer :all]))

(facts
  "clj->cljs works as expected"
  (tabular
    (fact
      "clj->cljs correctly maps namespaces"
      (clj->cljs ?code) => ?result)
      ?code                   ?result
      'symbol                 'symbol
      'clojure.core/fn        'cljs.core/fn
      'clojure.test/is        'cljs.test/is
      'unknown.namespace/func 'unknown.namespace/func)
  (fact
    "clj->cljs returns vector given vector"
    (clj->cljs [..symbol..]) => vector?)

  (fact
    "clj->cljs returns a list given a list"
    (clj->cljs '(..symbol..)) => seq?)

  (fact
    "clj->cljs handles nested namespaces"
    (clj->cljs
      '(clojure.core/func ['clojure.test/func2 '(1 2 3 'namespce/func3)])) => '(cljs.core/func ['cljs.test/func2 '(1 2 3 'namespce/func3)])))