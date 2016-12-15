(ns smidje.cljs-generator.test-builder
    (:require [smidje.arrows :refer :all]))

(defmulti generate-right-hand
  (fn [assertion]
    (if (:throws-exception assertion)
      :generate-expected-exception
      :generate-assertion)))

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

(defn generate-expected-exception [exception-definition]
  (let [expected-exception (:throws-exception exception-definition)
        call-form (:call-form exception-definition)]
    `(cljs.test/is (~'thrown? ~(symbol expected-exception) ~call-form))))

(defn generate-test [test-definition]
  (let [assertions# (:assertions test-definition)
        name#       (:name test-definition)]
    `(cljs.test/deftest ~(symbol name#)
       ~@(map generate-right-hand assertions#))))

(defn generate-tests [test-runtime]
  (let [tests# (:tests test-runtime)]
     `(do ~@(map generate-test tests#))))

(defmacro testmacro [test-runtime]
  (generate-tests test-runtime))



(defmethod generate-right-hand :generate-assertion
  [assertion]
  (generate-assertion assertion))

(defmethod generate-right-hand :generate-expected-exception
  [assertion]
  (generate-expected-exception assertion))
