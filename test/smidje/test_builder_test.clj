(ns smidje.test-builder-test
  (:require [midje.sweet :as m]
            [smidje.cljs-generator.test-builder :as builder]))

(def simple-exception
  '(cljs.test/is (thrown? Exception (throw Exception))))

(m/fact "simple throws"
  (builder/generate-expected-exception {:throws-exception 'Exception
                                        :call-form '(throw Exception)
                                        })
  => simple-exception)

(def simple-generated-test
  '(cljs.test/deftest foo-returns-2 (cljs.test/is (cljs.core/= (foo 1) 2))))

(def exception-test
  '(cljs.test/deftest foo-throws-exception
     (cljs.test/is (thrown? InvalidFooError (foo nil)))))

(m/fact "simple tests produce assert or exception forms"
  (builder/generate-test {:name "foo-returns-2"
                          :assertions [{:call-form '(foo 1)
                                        :arrow '=>
                                        :expected-result 2}]}) => simple-generated-test
  (builder/generate-test {:name "foo-throws-exception"
                          :assertions [{:call-form '(foo nil)
                                        :arrow '=>
                                        :throws-exception 'InvalidFooError}]}) => exception-test
  )
