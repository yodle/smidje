(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer :all]))

(defn do-arrow [arrow]
      (cond
        (= arrow '=>) 'cljs.core/=
        (= arrow '=not>) 'cljs.core/not=
        (= arrow '=not=>) 'cljs.core/not=
        :else (throw (Exception. (format "Unknown arrow given: %s | Valid arrows: %s"
                                         arrow
                                         arrow-set)))))

(defn generate-assertion [assertion]
      (let [{test-function#   :call-form
             expected-result# :expected-result
             arrow#           :arrow} assertion]
           `(cljs.test/is (~(do-arrow arrow#) ~test-function# ~expected-result#))))

(defn generate-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    `(cljs.test/deftest ~(symbol name#)
       ~@(map generate-assertion assertions#))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
     `(do ~@(map generate-test tests#))))

(defmacro testmacro [test-runtime]
  (generate-tests test-runtime))

(defn generate-expected-exception [exception-definition]
  (let [expected-exception (:throws-exception exception-definition)
        expected-result-form (:expected-result-form exception-definition)]
    `(cljs.test/is (cljs.test/thrown? ~(symbol expected-exception) ~expected-result-form))))
