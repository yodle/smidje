(ns smidje.test-builder-test
  (:require [midje.sweet :as m]
            [smidje.cljs-generator.test-builder :as builder]))

(def simple-exception
  '(cljs.test/is (cljs.test/thrown? Exception (throw Exception))))

(m/fact "simple throws"
  (builder/generate-expected-exception {:throws-exception 'Exception
                                        :expected-result-form '(throw Exception)
                                        })
  => simple-exception)
