(ns smidje.cljs-generator.test-builder
  (:require [clojure.test :refer :all]))

(defn generate-assertion [assertion]
  (let [test-function#   (:function-under-test assertion)
        expected-result# (:expected-result assertion)]
    `(is (= ~test-function# ~expected-result#))))

(defn generate-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    `(deftest ~(symbol name#)
       ~@(map generate-assertion assertions#))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
    `(do ~@(map generate-test tests#))))